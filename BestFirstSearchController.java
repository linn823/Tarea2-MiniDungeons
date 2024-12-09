package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import dungeon.play.GameCharacter;
import dungeon.play.PlayMap;
import util.math2d.Point2D;

public class BestFirstSearchController extends Controller {
    public final static int UP = 0;
    public final static int RIGHT = 1;
    public final static int DOWN = 2;
    public final static int LEFT = 3;
    public final static int IDLE = -1;

    private List<Point2D> finalPath;

    public BestFirstSearchController(PlayMap map, GameCharacter gameCharacter) {
        super(map, gameCharacter, "ZombieController");
        this.finalPath = findPath(map, map.getExit(0));
    }

    @Override
    public int getNextAction() {
        if (!finalPath.isEmpty()) {
            Point2D nextNode = finalPath.remove(0);
            Point2D heroPos = map.getHero().getPosition();

            if (nextNode.x == heroPos.x - 1 && map.isValidMove(nextNode)) return LEFT;
            else if (nextNode.x == heroPos.x + 1 && map.isValidMove(nextNode)) return RIGHT;
            else if (nextNode.y == heroPos.y - 1 && map.isValidMove(nextNode)) return UP;
            else if (nextNode.y == heroPos.y + 1 && map.isValidMove(nextNode)) return DOWN;
        }
        
        return IDLE;
    }

    public List<Point2D> findPath(PlayMap map, Point2D start) {
        Set<String> visited = new HashSet<>();
        PriorityQueue<Point2D> queue = new PriorityQueue<>(Comparator.comparingInt(a -> manhattanDistance(a, map.getExit(1))));
        Map<Point2D, Point2D> parentMap = new HashMap<>();

        queue.add(start);
        while (!queue.isEmpty()) {
            Point2D current = queue.poll();
            if (map.isExit((int) current.x, (int) current.y)) { // devolver el camino
                return reconstructPath(current, map.getExit(0), parentMap);
            }

            visited.add(current.toString());
            for (Point2D neighbor : getNeighbors(current, map)) {
                if (!visited.contains(neighbor.toString())) {
                    queue.add(neighbor);
                    parentMap.put(neighbor, current);
                }
            }
        }
        return Collections.emptyList();
    }

    private List<Point2D> reconstructPath(Point2D exit, Point2D start, Map<Point2D, Point2D> parentMap) {
        List<Point2D> path = new ArrayList<>();
        Point2D current = exit;
        while (!current.equals(start)) {
            path.add(current);
            current = parentMap.get(current);
        }
        path.add(start);
        Collections.reverse(path);
        return path;
    }

    private List<Point2D> getNeighbors(Point2D position, PlayMap map) {
        List<Point2D> neighbors = new ArrayList<>();
        int x = (int) position.x;
        int y = (int) position.y;

        if (map.isValidMove(x + 1, y)) {
            neighbors.add(new Point2D(x + 1, y));
        }
        if (map.isValidMove(x - 1, y)) {
            neighbors.add(new Point2D(x - 1, y));
        }
        if (map.isValidMove(x, y + 1)) {
            neighbors.add(new Point2D(x, y + 1));
        }
        if (map.isValidMove(x, y - 1)) {
            neighbors.add(new Point2D(x, y - 1));
        }

        return neighbors;
    }

    private int manhattanDistance(Point2D point1, Point2D point2) {
        return Math.abs((int) point1.x - (int) point2.x) + Math.abs((int) point1.y - (int) point2.y);
    }
}
