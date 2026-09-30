import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Environment {

    private final int width;
    private final int height;
    private final Agent[][] grid;

    private final List<Agent> agents = new ArrayList<>();
    private final List<Agent> newborns = new ArrayList<>();
    private final List<Agent> dead = new ArrayList<>();

    private final Random random = new Random();

    private int step;

    public Environment(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "Размеры поля должны быть положительными."
            );
        }

        this.width = width;
        this.height = height;
        this.grid = new Agent[height][width];
        this.step = 0;
    }

    // Основные параметры

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getStep() {
        return step;
    }

    public Random getRandom() {
        return random;
    }

    // ГРАНИЦЫ ПОЛЯ

    /**
     * Проверяет, находится ли клетка внутри поля.
     *
     * Левая граница: x = 0
     * Правая граница: x = width - 1
     * Верхняя граница: y = 0
     * Нижняя граница: y = height - 1
     */
    public boolean isInside(int x, int y) {
        return x >= 0
                && x < width
                && y >= 0
                && y < height;
    }

    // Работа с grid

    public Agent getAgent(int x, int y) {

        // За границами поля ничего нет.
        if (!isInside(x, y)) {
            return null;
        }

        return grid[y][x];
    }

    public boolean setAgent(int x, int y, Agent agent) {

        if (!isInside(x, y)) {
            return false;
        }

        Agent current = grid[y][x];

        if (current != null
                && agent != null
                && current != agent) {
            return false;
        }

        grid[y][x] = agent;

        if (agent != null) {
            agent.setPosition(x, y);
        }

        return true;
    }

    public boolean addAgent(Agent agent, int x, int y) {

        if (agent == null) {
            return false;
        }

        if (!isInside(x, y)) {
            return false;
        }

        if (grid[y][x] != null) {
            return false;
        }

        grid[y][x] = agent;
        agent.setPosition(x, y);

        agents.add(agent);

        return true;
    }

    public void addNewborn(Agent child, int x, int y) {

        if (child == null) {
            return;
        }

        if (!isInside(x, y)) {
            return;
        }

        if (grid[y][x] != null) {
            return;
        }

        grid[y][x] = child;
        child.setPosition(x, y);

        newborns.add(child);
    }

    public Agent removeAgent(int x, int y) {

        if (!isInside(x, y)) {
            return null;
        }

        Agent removed = grid[y][x];

        grid[y][x] = null;

        return removed;
    }

    // ДВИЖЕНИЕ НА 1 КЛЕТКУ

    /**
     * Передвижение строго на одну клетку
     * по горизонтали или вертикали.
     *
     * За границы поля выйти нельзя.
     */
    public boolean moveAgent(
            Agent agent,
            int newX,
            int newY,
            int energyCost
    ) {

        if (agent == null
                || agent.isDead()
                || !isAliveOnGrid(agent)) {
            return false;
        }

        if (!isInside(newX, newY)) {
            return false;
        }

        if (energyCost < 0) {
            return false;
        }

        if (agent.getEnergy() < energyCost) {
            return false;
        }

        int dx = Math.abs(
                newX - agent.getX()
        );

        int dy = Math.abs(
                newY - agent.getY()
        );

        // Разрешено только перемещение на одну клетку:
        // вверх / вниз / влево / вправо.
        if (dx + dy != 1) {
            return false;
        }

        if (grid[newY][newX] != null) {
            return false;
        }

        grid[agent.getY()][agent.getX()] = null;

        grid[newY][newX] = agent;

        agent.setPosition(
                newX,
                newY
        );

        // Энергия за фактическое перемещение.
        agent.changeEnergy(-energyCost);

        return true;
    }

    // Поедание растения

    public boolean moveIntoFood(
            Agent eater,
            Agent food,
            int energyCost,
            int foodEnergy
    ) {

        if (eater == null
                || food == null
                || !isAliveOnGrid(eater)
                || !isAliveOnGrid(food)) {
            return false;
        }

        if (eater.getEnergy() < energyCost) {
            return false;
        }

        int targetX = food.getX();
        int targetY = food.getY();

        // Растение должно находиться ровно в соседней клетке.
        if (getManhattanDistance(
                eater.getX(),
                eater.getY(),
                targetX,
                targetY
        ) != 1) {
            return false;
        }

        grid[eater.getY()][eater.getX()] = null;

        grid[targetY][targetX] = eater;

        eater.setPosition(
                targetX,
                targetY
        );

        // Стоимость перемещения.
        eater.changeEnergy(-energyCost);

        // Энергия от пищи.
        eater.changeEnergy(foodEnergy);

        markDead(food);

        return true;
    }

    // Поедание травоядного

    public boolean moveIntoPrey(
            Agent hunter,
            Herbivore prey,
            int energyCost,
            int foodEnergy
    ) {

        if (hunter == null
                || prey == null
                || !isAliveOnGrid(hunter)
                || !isAliveOnGrid(prey)) {
            return false;
        }

        if (hunter.getEnergy() < energyCost) {
            return false;
        }

        int targetX = prey.getX();
        int targetY = prey.getY();

        // Атака происходит только из соседней клетки.
        if (getManhattanDistance(
                hunter.getX(),
                hunter.getY(),
                targetX,
                targetY
        ) != 1) {
            return false;
        }

        grid[hunter.getY()][hunter.getX()] = null;

        markDead(prey);

        grid[targetY][targetX] = hunter;

        hunter.setPosition(
                targetX,
                targetY
        );

        // Стоимость движения / атаки.
        hunter.changeEnergy(-energyCost);

        // Энергия от добычи.
        hunter.changeEnergy(foodEnergy);

        return true;
    }

    // Смерть

    public void markDead(Agent agent) {

        if (agent == null) {
            return;
        }

        if (dead.contains(agent)) {
            return;
        }

        dead.add(agent);

        if (isInside(
                agent.getX(),
                agent.getY()
        ) && grid[agent.getY()][agent.getX()] == agent) {

            grid[agent.getY()][agent.getX()] = null;
        }
    }

    private boolean isAliveOnGrid(Agent agent) {

        return agent != null
                && !agent.isDead()
                && !dead.contains(agent)
                && isInside(
                agent.getX(),
                agent.getY()
        )
                && grid[agent.getY()][agent.getX()] == agent;
    }

    // Один шаг симуляции

    public void step() {

        for (int i = 0; i < agents.size(); i++) {

            Agent agent = agents.get(i);

            if (isAliveOnGrid(agent)) {
                agent.act();
            }
        }

        for (Agent agent : agents) {

            if (agent.isDead()) {
                markDead(agent);
            }
        }

        agents.removeAll(dead);

        /*
         * Рождённый агент может быть съеден
         * до конца текущего хода.
         *
         * Поэтому перед добавлением потомков
         * в основной список удаляем уже погибших
         * новорождённых.
         */
        newborns.removeAll(dead);

        agents.addAll(newborns);

        newborns.clear();
        dead.clear();

        step++;
    }

    // Поиск соседей

    /**
     * Возвращает клетки вокруг заданной позиции.
     *
     * Никакого перехода через противоположную
     * границу поля нет.
     */
    public List<int[]> getNeighbors(
            int x,
            int y,
            int radius
    ) {

        if (radius < 0) {
            return Collections.emptyList();
        }

        List<int[]> result = new ArrayList<>();

        for (int dy = -radius; dy <= radius; dy++) {

            for (int dx = -radius; dx <= radius; dx++) {

                if (dx == 0 && dy == 0) {
                    continue;
                }

                int neighborX = x + dx;
                int neighborY = y + dy;

                if (!isInside(
                        neighborX,
                        neighborY
                )) {
                    continue;
                }

                result.add(
                        new int[]{
                                neighborX,
                                neighborY
                        }
                );
            }
        }

        return result;
    }

    // Случайный свободный сосед

    public int[] findRandomFreeNeighbor(
            int x,
            int y
    ) {

        List<int[]> neighbors =
                getNeighbors(x, y, 1);

        if (neighbors.isEmpty()) {
            return null;
        }

        int start =
                random.nextInt(
                        neighbors.size()
                );

        for (int i = 0; i < neighbors.size(); i++) {

            int[] cell =
                    neighbors.get(
                            (start + i)
                                    % neighbors.size()
                    );

            if (getAgent(
                    cell[0],
                    cell[1]
            ) == null) {

                return cell;
            }
        }

        return null;
    }

    // Обычные расстояния

    public int deltaX(
            int fromX,
            int toX
    ) {
        return toX - fromX;
    }

    public int deltaY(
            int fromY,
            int toY
    ) {
        return toY - fromY;
    }

    public int getManhattanDistance(
            int x1,
            int y1,
            int x2,
            int y2
    ) {

        return Math.abs(x2 - x1)
                + Math.abs(y2 - y1);
    }

    // Списки агентов

    public List<Agent> getAllAgents() {
        return Collections.unmodifiableList(agents);
    }

    // Статистика количества

    public int getAgentCount() {
        return agents.size();
    }

    public int getPlantCount() {
        return countType(Plant.class);
    }

    public int getHerbivoreCount() {
        return countType(Herbivore.class);
    }

    public int getPredatorCount() {
        return countType(Predator.class);
    }

    private int countType(Class<?> type) {

        int count = 0;

        for (Agent agent : agents) {

            if (!agent.isDead()
                    && type.isInstance(agent)) {

                count++;
            }
        }

        return count;
    }
}
