package controllers;

import java.util.Random;
import dungeon.play.GameCharacter;
import dungeon.play.PlayMap;
import util.math2d.Point2D;

public class QLearningController extends Controller {
    private float[][][] qTable; // Q-Table [x][y][action]
    private Random randomGenerator;
    private float explorationChance = 0.9f;
    private float gammaValue = 0.9f;
    private float learningRate = 0.1f;
    private int actionRange = 5;
    private int numTrainingEpisodes = 1000;
    private int maxStepsPerEpisode = 300;

    public QLearningController(PlayMap map, GameCharacter controllingChar) {
        super(map, controllingChar, "ZombieController");
        randomGenerator = new Random();
        initializeQTable();
        explorationChance = 0.9f;
        trainAgent(); // Entrenar agente
        map.startGame(); // resetear mapa al terminar de entrenar
    }

    private void initializeQTable() {
        qTable = new float[map.getMapSizeX()][map.getMapSizeY()][actionRange];
        for (int x = 0; x < map.getMapSizeX(); x++) {
            for (int y = 0; y < map.getMapSizeY(); y++) {
                for (int action = 0; action < actionRange; action++) {
                    qTable[x][y][action] = 0.0f;
                }
            }
        }
    }

    private void trainAgent() {
        
        for (int episode = 0; episode < numTrainingEpisodes; episode++) {
            map.startGame();
            
            Point2D currentState = map.getHero().getPosition();
            int xPos = (int)currentState.x;
            int yPos = (int)currentState.y;
            
            int steps = 0;
            while (!map.isExit(xPos, yPos) && steps < maxStepsPerEpisode) {
                int action = selectActionWithExploration(currentState);
                
                //System.out.println(action);
                Point2D nextState = map.getHero().getNextPosition(action);
                if (!map.isValidMove(nextState)) {
                    continue;
                }

                float reward = calculateReward(nextState);
                int nextX = (int) nextState.x;
                int nextY = (int) nextState.y;

                float maxNextQ = getMaxQValue(nextX, nextY);
                qTable[xPos][yPos][action] += learningRate * (reward + gammaValue * maxNextQ - qTable[xPos][yPos][action]);

                map.updateGame(action);
                
                xPos = nextX;
                yPos = nextY;
                currentState = new Point2D(xPos, yPos);
                steps++;
            }
        }
    }

    private int selectActionWithExploration(Point2D state) {
        if (randomGenerator.nextFloat() < explorationChance) {
            return randomGenerator.nextInt(actionRange);
        } else {
            return getBestAction(state);
        }
    }

    public int getNextAction() {
        Point2D heroPosition = map.getHero().getPosition();
        return getBestAction(heroPosition);
    }

    private int getBestAction(Point2D state) {
        float[] qValues = qTable[(int) state.x][(int) state.y];
        float bestValue = qValues[0];
        int bestAction = 0;
        for (int action = 1; action < qValues.length; action++) {
            if (qValues[action] > bestValue) {
                bestValue = qValues[action];
                bestAction = action;
            }
        }
        return bestAction;
    }

    private float calculateReward(Point2D nextState) {
        if (map.isExit((int) nextState.x, (int) nextState.y)) {
            return 100.0f; 
        }
        if (map.getUnexplored()[(int) nextState.x][(int) nextState.y]) {
            return 10.0f; 
        }
        return -1.0f;
    }

    private float getMaxQValue(int x, int y) {
        float maxQ = qTable[x][y][0];
        for (int action = 1; action < actionRange; action++) {
            if (qTable[x][y][action] > maxQ) {
                maxQ = qTable[x][y][action];
            }
        }
        return maxQ;
    }
}
