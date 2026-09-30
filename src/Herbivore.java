public class Herbivore extends Agent {

    public static final int INITIAL_ENERGY = 30;
    public static final int MAX_ENERGY = 65;

    private static final int TURN_ENERGY_COST = 1;
    private static final int MOVE_COST = 1;

    private static final int FOOD_ENERGY = 5;

    private static final int REPRODUCTION_THRESHOLD = 52;

    private static final int OFFSPRING_ENERGY = 20;

    private static final int VISION_RADIUS = 2;

    public Herbivore(Environment env, int x, int y) {
        super(env, x, y, INITIAL_ENERGY, 'Т');
    }

    @Override
    public int getMaxEnergy() {
        return MAX_ENERGY;
    }

    @Override
    protected int getTurnEnergyCost() {
        return TURN_ENERGY_COST;
    }

    @Override
    protected int getReproductionThreshold() {
        return REPRODUCTION_THRESHOLD;
    }

    @Override
    protected int getOffspringEnergy() {
        return OFFSPRING_ENERGY;
    }

    @Override
    public void act() {

        if (!beginTurn()) {
            return;
        }

        Predator nearestPredator = findNearestPredator();

        Plant nearestPlant = findNearestPlant();

        /*
         * Приоритет:
         *
         * 1. Побег от хищника
         * 2. Путь к растению
         * 3. Случайное движение
         * 4. Размножение
         *
         * Движение всегда только на одну клетку.
         */

        if (nearestPredator != null) {

            flee();

        } else if (nearestPlant != null) {

            moveToPlant(nearestPlant);

        } else {

            wander();
        }

        tryReproduce();
    }

    // Зрение

    private Predator findNearestPredator() {

        Predator best = null;

        int bestDistance = Integer.MAX_VALUE;

        for (int dy = -VISION_RADIUS; dy <= VISION_RADIUS; dy++) {

            for (int dx = -VISION_RADIUS; dx <= VISION_RADIUS; dx++) {

                if (dx == 0 && dy == 0) {
                    continue;
                }

                Agent agent = env.getAgent(getX() + dx, getY() + dy);

                if (agent instanceof Predator) {

                    int distance = env.getManhattanDistance(getX(), getY(), agent.getX(), agent.getY());

                    if (distance < bestDistance) {

                        bestDistance = distance;

                        best = (Predator) agent;
                    }
                }
            }
        }

        return best;
    }

    private Plant findNearestPlant() {

        Plant best = null;

        int bestDistance = Integer.MAX_VALUE;

        for (int dy = -VISION_RADIUS; dy <= VISION_RADIUS; dy++) {

            for (int dx = -VISION_RADIUS; dx <= VISION_RADIUS; dx++) {

                if (dx == 0 && dy == 0) {
                    continue;
                }

                Agent agent = env.getAgent(getX() + dx, getY() + dy);

                if (agent instanceof Plant) {

                    int distance = env.getManhattanDistance(getX(), getY(), agent.getX(), agent.getY());

                    if (distance < bestDistance) {

                        bestDistance = distance;

                        best = (Plant) agent;
                    }
                }
            }
        }

        return best;
    }

    // Побег

    private void flee() {

        Predator predator = findNearestPredator();

        if (predator == null) {
            wander();
            return;
        }

        // Теперь побег всегда только на одну клетку.
        moveBestEscape();
    }

    /**
     * Выбирает наиболее безопасную соседнюю клетку.
     * <p>
     * Клетка за пределами поля автоматически исключается.
     */
    private boolean moveBestEscape() {

        int bestX = getX();
        int bestY = getY();

        int bestMinDistance = Integer.MIN_VALUE;
        int bestTotalDistance = Integer.MIN_VALUE;

        boolean found = false;

        final int[] dirX = {0, 0, -1, 1};
        final int[] dirY = {-1, 1, 0, 0};

        for (int i = 0; i < 4; i++) {

            int newX = getX() + dirX[i];
            int newY = getY() + dirY[i];

            // Жёсткая граница поля.
            if (!env.isInside(newX, newY)) {
                continue;
            }

            Agent targetAgent = env.getAgent(newX, newY);

            /*
             * При побеге свободная клетка или растение
             * являются допустимыми вариантами.
             *
             * Другие агенты являются препятствием.
             */
            if (targetAgent != null && !(targetAgent instanceof Plant)) {
                continue;
            }

            int minDistance = Integer.MAX_VALUE;
            int totalDistance = 0;

            /*
             * Повторно анализируем зрение 5x5
             * относительно предполагаемой новой позиции.
             */
            for (int dy = -VISION_RADIUS; dy <= VISION_RADIUS; dy++) {

                for (int dx = -VISION_RADIUS; dx <= VISION_RADIUS; dx++) {

                    int scanX = newX + dx;
                    int scanY = newY + dy;

                    Agent agent = env.getAgent(scanX, scanY);

                    if (agent instanceof Predator) {

                        int predatorDistance = env.getManhattanDistance(newX, newY, agent.getX(), agent.getY());

                        minDistance = Math.min(minDistance, predatorDistance);

                        totalDistance += predatorDistance;
                    }
                }
            }

            if (minDistance == Integer.MAX_VALUE) {
                minDistance = 0;
            }

            /*
             * Сначала выбираем клетку,
             * где ближайший хищник находится дальше.
             *
             * При равенстве рассматриваем суммарную
             * дистанцию до видимых хищников.
             */
            if (!found || minDistance > bestMinDistance || (minDistance == bestMinDistance && totalDistance > bestTotalDistance)) {

                found = true;

                bestX = newX;
                bestY = newY;

                bestMinDistance = minDistance;

                bestTotalDistance = totalDistance;
            }
        }

        if (!found) {
            return false;
        }

        Agent targetAgent = env.getAgent(bestX, bestY);

        if (targetAgent instanceof Plant) {

            return env.moveIntoFood(this, targetAgent, MOVE_COST, FOOD_ENERGY);
        }

        return env.moveAgent(this, bestX, bestY, MOVE_COST);
    }

    // Движение к растению

    private void moveToPlant(Plant target) {

        int distance = env.getManhattanDistance(getX(), getY(), target.getX(), target.getY());

        /*
         * Растение рядом:
         * заходим в его клетку и съедаем.
         */
        if (distance == 1) {

            env.moveIntoFood(this, target, MOVE_COST, FOOD_ENERGY);

            return;
        }

        int bestX = -1;
        int bestY = -1;

        int bestDistance = Integer.MAX_VALUE;

        final int[] dirX = {0, 0, -1, 1};
        final int[] dirY = {-1, 1, 0, 0};

        for (int i = 0; i < 4; i++) {

            int newX = getX() + dirX[i];
            int newY = getY() + dirY[i];

            // Выход за границу запрещён.
            if (!env.isInside(newX, newY)) {
                continue;
            }

            if (env.getAgent(newX, newY) != null) {
                continue;
            }

            int candidateDistance = env.getManhattanDistance(newX, newY, target.getX(), target.getY());

            if (candidateDistance < bestDistance) {

                bestDistance = candidateDistance;

                bestX = newX;
                bestY = newY;
            }
        }

        if (bestX >= 0) {

            env.moveAgent(this, bestX, bestY, MOVE_COST);

        } else {

            wander();
        }
    }

    // Случайное движение

    private void wander() {

        final int[] dirX = {0, 0, -1, 1};
        final int[] dirY = {-1, 1, 0, 0};

        int start = env.getRandom().nextInt(4);

        for (int i = 0; i < 4; i++) {

            int index = (start + i) % 4;

            int newX = getX() + dirX[index];
            int newY = getY() + dirY[index];

            // Жёсткая граница.
            if (!env.isInside(newX, newY)) {
                continue;
            }

            if (env.getAgent(newX, newY) == null) {

                env.moveAgent(this, newX, newY, MOVE_COST);

                return;
            }
        }
    }

    // Рождение

    @Override
    protected Agent createOffspring(int x, int y) {
        return new Herbivore(env, x, y);
    }
}