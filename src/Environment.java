import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Environment {

    /*
     * Размеры поля.
     */
    private final int width;
    private final int height;

    /*
     * Двумерная сетка.
     *
     * Первый индекс = Y
     * Второй индекс = X
     */
    private final Agent[][] grid;

    /*
     * Основной список живущих агентов,
     * участвующих в текущей симуляции.
     */
    private final List<Agent> agents = new ArrayList<>();

    /*
     * Новые агенты.
     *
     * Они создаются в текущем ходу,
     * но начинают действовать только
     * со следующего полного хода.
     */
    private final List<Agent> newborns = new ArrayList<>();

    /*
     * Агенты, которые должны быть удалены
     * после завершения шага.
     */
    private final List<Agent> dead = new ArrayList<>();

    /*
     * Генератор случайных чисел.
     */
    private final Random random = new Random();

    /*
     * Текущий номер шага.
     */
    private int step;

    /*
     * ==========================================
     * КОНСТРУКТОР
     * ==========================================
     */

    public Environment(int width, int height) {

        if (width <= 0 || height <= 0) {

            throw new IllegalArgumentException("Размеры поля должны быть положительными.");
        }

        this.width = width;
        this.height = height;

        this.grid = new Agent[height][width];

        this.step = 0;
    }

    /*
     * ==========================================
     * ОСНОВНЫЕ ПАРАМЕТРЫ
     * ==========================================
     */

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

    /*
     * ==========================================
     * ГРАНИЦЫ ПОЛЯ
     * ==========================================
     *
     * ВАЖНО:
     * поле НЕ является тороидальным.
     *
     * x:
     * 0 ... width - 1
     *
     * y:
     * 0 ... height - 1
     */

    public boolean isInside(int x, int y) {

        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /*
     * ==========================================
     * GRID
     * ==========================================
     */

    public Agent getAgent(int x, int y) {

        /*
         * За пределами поля
         * агента нет.
         */
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

        /*
         * Нельзя заменить одного агента
         * другим в занятой клетке.
         *
         * Можно:
         * - поставить агента в пустую клетку;
         * - удалить агента;
         * - установить того же самого агента.
         */
        if (current != null && agent != null && current != agent) {

            return false;
        }

        grid[y][x] = agent;

        if (agent != null) {

            agent.setPosition(x, y);
        }

        return true;
    }

    /*
     * ==========================================
     * ДОБАВЛЕНИЕ АГЕНТА
     * ==========================================
     */

    public boolean addAgent(Agent agent, int x, int y) {

        if (agent == null) {
            return false;
        }

        if (!isInside(x, y)) {
            return false;
        }

        /*
         * Клетка уже занята.
         */
        if (grid[y][x] != null) {
            return false;
        }

        grid[y][x] = agent;

        agent.setPosition(x, y);

        agents.add(agent);

        return true;
    }

    /*
     * ==========================================
     * ДОБАВЛЕНИЕ НОВОРОЖДЕННОГО
     * ==========================================
     */

    public void addNewborn(Agent child, int x, int y) {

        if (child == null) {
            return;
        }

        if (!isInside(x, y)) {
            return;
        }

        /*
         * Если клетка уже занята,
         * рождение не происходит.
         */
        if (grid[y][x] != null) {
            return;
        }

        grid[y][x] = child;

        child.setPosition(x, y);

        newborns.add(child);
    }

    /*
     * ==========================================
     * УДАЛЕНИЕ АГЕНТА
     * ==========================================
     */

    public Agent removeAgent(int x, int y) {

        if (!isInside(x, y)) {
            return null;
        }

        Agent removed = grid[y][x];

        grid[y][x] = null;

        return removed;
    }

    /*
     * ==========================================
     * ОБЫЧНОЕ ДВИЖЕНИЕ НА 1 КЛЕТКУ
     * ==========================================
     *
     * Только:
     *
     * вверх
     * вниз
     * влево
     * вправо
     *
     * Диагонали запрещены.
     */

    public boolean moveAgent(Agent agent, int newX, int newY, int energyCost) {

        if (agent == null || agent.isDead() || !isAliveOnGrid(agent)) {

            return false;
        }

        if (!isInside(newX, newY)) {
            return false;
        }

        if (energyCost < 0) {
            return false;
        }

        /*
         * Не хватает энергии на движение.
         */
        if (agent.getEnergy() < energyCost) {
            return false;
        }

        /*
         * Разница координат.
         */
        int dx = Math.abs(newX - agent.getX());

        int dy = Math.abs(newY - agent.getY());

        /*
         * Ровно одна клетка
         * по одной оси.
         */
        if (dx + dy != 1) {
            return false;
        }

        /*
         * В занятую клетку зайти нельзя.
         */
        if (grid[newY][newX] != null) {
            return false;
        }

        /*
         * Освобождаем старую клетку.
         */
        grid[agent.getY()][agent.getX()] = null;

        /*
         * Занимаем новую.
         */
        grid[newY][newX] = agent;

        agent.setPosition(newX, newY);

        /*
         * Стоимость движения.
         */
        agent.changeEnergy(-energyCost);

        return true;
    }

    /*
     * ==========================================
     * ПОЕДАНИЕ РАСТЕНИЯ
     * ==========================================
     *
     * Травоядное входит в соседнюю клетку
     * растения, платит стоимость движения,
     * получает энергию и растение погибает.
     */

    public boolean moveIntoFood(Agent eater, Agent food, int energyCost, int foodEnergy) {

        if (eater == null || food == null || !isAliveOnGrid(eater) || !isAliveOnGrid(food)) {

            return false;
        }

        /*
         * Энергии должно хватить
         * на движение.
         */
        if (eater.getEnergy() < energyCost) {
            return false;
        }

        int targetX = food.getX();

        int targetY = food.getY();

        /*
         * Растение должно быть
         * ровно в соседней клетке.
         */
        if (getManhattanDistance(eater.getX(), eater.getY(), targetX, targetY) != 1) {

            return false;
        }

        /*
         * Убираем едящего агента
         * со старой позиции.
         */
        grid[eater.getY()][eater.getX()] = null;

        /*
         * Перемещаем его на клетку
         * растения.
         */
        grid[targetY][targetX] = eater;

        eater.setPosition(targetX, targetY);

        /*
         * Стоимость движения.
         */
        eater.changeEnergy(-energyCost);

        /*
         * Энергия пищи.
         */
        eater.changeEnergy(foodEnergy);

        /*
         * Растение съедено.
         */
        markDead(food);

        return true;
    }

    /*
     * ==========================================
     * ПОЕДАНИЕ ТРАВОЯДНОГО
     * ==========================================
     *
     * Хищник атакует только соседнего
     * травоядного.
     *
     * Никакого двухклеточного прыжка:
     * именно так работает предоставленная
     * консольная версия.
     */

    public boolean moveIntoPrey(Agent hunter, Herbivore prey, int energyCost, int foodEnergy) {

        if (hunter == null || prey == null || !isAliveOnGrid(hunter) || !isAliveOnGrid(prey)) {

            return false;
        }

        /*
         * Энергии должно хватить
         * на атаку.
         */
        if (hunter.getEnergy() < energyCost) {
            return false;
        }

        int targetX = prey.getX();

        int targetY = prey.getY();

        /*
         * Атака только из соседней клетки.
         */
        if (getManhattanDistance(hunter.getX(), hunter.getY(), targetX, targetY) != 1) {

            return false;
        }

        /*
         * Убираем хищника с прежней клетки.
         */
        grid[hunter.getY()][hunter.getX()] = null;

        /*
         * Жертва погибает.
         */
        markDead(prey);

        /*
         * Хищник занимает клетку жертвы.
         */
        grid[targetY][targetX] = hunter;

        hunter.setPosition(targetX, targetY);

        /*
         * Стоимость атаки.
         */
        hunter.changeEnergy(-energyCost);

        /*
         * Полученная энергия.
         */
        hunter.changeEnergy(foodEnergy);

        return true;
    }

    /*
     * ==========================================
     * СМЕРТЬ
     * ==========================================
     */

    public void markDead(Agent agent) {

        if (agent == null) {
            return;
        }

        /*
         * Уже находится среди погибших.
         */
        if (dead.contains(agent)) {
            return;
        }

        dead.add(agent);

        /*
         * Если объект всё ещё стоит
         * именно на своей клетке,
         * освобождаем её.
         */
        if (isInside(agent.getX(), agent.getY()) && grid[agent.getY()][agent.getX()] == agent) {

            grid[agent.getY()][agent.getX()] = null;
        }
    }

    /*
     * Проверка:
     *
     * - объект существует;
     * - не мёртв;
     * - не находится в dead;
     * - координаты внутри поля;
     * - grid действительно содержит
     *   именно этого агента.
     */
    private boolean isAliveOnGrid(Agent agent) {

        return agent != null && !agent.isDead() && !dead.contains(agent) && isInside(agent.getX(), agent.getY()) && grid[agent.getY()][agent.getX()] == agent;
    }

    /*
     * ==========================================
     * ОДИН ШАГ СИМУЛЯЦИИ
     * ==========================================
     *
     * Порядок полностью соответствует
     * консольной версии.
     */

    public void step() {

        /*
         * Сначала ходят все агенты,
         * которые существовали
         * до начала этого шага.
         */
        for (int i = 0; i < agents.size(); i++) {

            Agent agent = agents.get(i);

            if (isAliveOnGrid(agent)) {
                agent.act();
            }
        }

        /*
         * Отдельно проверяем смерть.
         */
        for (Agent agent : agents) {

            if (agent.isDead()) {
                markDead(agent);
            }
        }

        /*
         * Удаляем погибших
         * из основного списка.
         */
        agents.removeAll(dead);

        /*
         * Теоретически новорожденный
         * мог быть съеден в этом же ходу.
         */
        newborns.removeAll(dead);

        /*
         * Теперь выжившие потомки
         * становятся полноценными агентами.
         */
        agents.addAll(newborns);

        /*
         * Очищаем временные списки.
         */
        newborns.clear();
        dead.clear();

        /*
         * Переходим к следующему шагу.
         */
        step++;
    }

    /*
     * ==========================================
     * СОСЕДИ
     * ==========================================
     *
     * Никакого перехода через край поля.
     */

    public List<int[]> getNeighbors(int x, int y, int radius) {

        if (radius < 0) {
            return Collections.emptyList();
        }

        List<int[]> result = new ArrayList<>();

        for (int dy = -radius; dy <= radius; dy++) {

            for (int dx = -radius; dx <= radius; dx++) {

                /*
                 * Центральную клетку
                 * не добавляем.
                 */
                if (dx == 0 && dy == 0) {
                    continue;
                }

                int neighborX = x + dx;

                int neighborY = y + dy;

                /*
                 * За границей клетки нет.
                 */
                if (!isInside(neighborX, neighborY)) {
                    continue;
                }

                result.add(new int[]{neighborX, neighborY});
            }
        }

        return result;
    }

    /*
     * ==========================================
     * СЛУЧАЙНЫЙ СВОБОДНЫЙ СОСЕД
     * ==========================================
     *
     * Используется при размножении.
     */

    public int[] findRandomFreeNeighbor(int x, int y) {

        List<int[]> neighbors = getNeighbors(x, y, 1);

        if (neighbors.isEmpty()) {
            return null;
        }

        /*
         * Начинаем со случайного соседа,
         * затем проверяем остальные.
         */
        int start = random.nextInt(neighbors.size());

        for (int i = 0; i < neighbors.size(); i++) {

            int[] cell = neighbors.get((start + i) % neighbors.size());

            if (getAgent(cell[0], cell[1]) == null) {

                return cell;
            }
        }

        return null;
    }

    /*
     * ==========================================
     * РАССТОЯНИЯ
     * ==========================================
     */

    public int deltaX(int fromX, int toX) {

        return toX - fromX;
    }

    public int deltaY(int fromY, int toY) {

        return toY - fromY;
    }

    /*
     * Манхэттенское расстояние.
     *
     * Именно оно используется
     * логикой зрения и движения
     * консольной версии.
     */
    public int getManhattanDistance(int x1, int y1, int x2, int y2) {

        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }

    /*
     * ==========================================
     * СПИСОК АГЕНТОВ
     * ==========================================
     */

    public List<Agent> getAllAgents() {

        return Collections.unmodifiableList(agents);
    }

    /*
     * ==========================================
     * СТАТИСТИКА
     * ==========================================
     */

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

            if (!agent.isDead() && type.isInstance(agent)) {

                count++;
            }
        }

        return count;
    }
}