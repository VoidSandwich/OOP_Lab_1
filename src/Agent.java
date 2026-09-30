public abstract class Agent {

    /*
     * Координаты агента.
     */
    private int x;
    private int y;

    /*
     * Текущая энергия.
     */
    private int energy;

    /*
     * Символьный идентификатор.
     * Нужен в первую очередь для совместимости
     * с логикой консольной версии.
     */
    private final char symbol;

    /*
     * Среда, в которой находится агент.
     */
    protected final Environment env;

    protected Agent(Environment env, int x, int y, int energy, char symbol) {

        if (env == null) {
            throw new IllegalArgumentException("Среда не может быть null.");
        }

        /*
         * В новой оконной версии логика среды
         * полностью соответствует консольной:
         * никаких телепортов через границу поля.
         */
        if (!env.isInside(x, y)) {
            throw new IllegalArgumentException("Координаты агента находятся за пределами поля.");
        }

        this.env = env;

        this.x = x;
        this.y = y;

        /*
         * Начальная энергия сразу ограничивается
         * допустимым максимумом конкретного вида.
         */
        this.energy = Math.max(0, Math.min(energy, getMaxEnergy()));

        this.symbol = symbol;
    }

    /*
     * ==========================================
     * GETTERS
     * ==========================================
     */

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getEnergy() {
        return energy;
    }

    public char getSymbol() {
        return symbol;
    }

    /*
     * Emoji для графического интерфейса.
     *
     * Конкретные типы агентов смогут
     * переопределять этот метод.
     */
    public String getEmoji() {
        return "❓";
    }

    /*
     * ==========================================
     * ПОЗИЦИЯ
     * ==========================================
     */

    public void setPosition(int x, int y) {

        if (!env.isInside(x, y)) {
            throw new IllegalArgumentException("Нельзя установить позицию за пределами поля.");
        }

        this.x = x;
        this.y = y;
    }

    /*
     * ==========================================
     * ЭНЕРГИЯ
     * ==========================================
     */

    public void setEnergy(int energy) {

        this.energy = Math.max(0, Math.min(energy, getMaxEnergy()));
    }

    public void changeEnergy(int delta) {

        setEnergy(energy + delta);
    }

    public boolean isDead() {
        return energy <= 0;
    }

    /*
     * ==========================================
     * НАЧАЛО ХОДА
     * ==========================================
     *
     * Для травоядных и хищников:
     *
     * 1. Проверяется жизнь агента.
     * 2. Списывается стоимость метаболизма.
     * 3. Если энергия закончилась —
     *    агент умирает.
     *
     * Для растения getTurnEnergyCost() = 0.
     */
    protected boolean beginTurn() {

        /*
         * Агент уже мёртв.
         */
        if (isDead()) {

            env.markDead(this);

            return false;
        }

        /*
         * Расход энергии за ход.
         */
        changeEnergy(-getTurnEnergyCost());

        /*
         * Проверяем смерть после метаболизма.
         */
        if (isDead()) {

            env.markDead(this);

            return false;
        }

        return true;
    }

    /*
     * ==========================================
     * РАЗМНОЖЕНИЕ
     * ==========================================
     *
     * Логика полностью соответствует
     * консольной версии:
     *
     * если энергия достигла порога,
     * ищется случайная свободная соседняя клетка.
     *
     * Ребёнок получает getOffspringEnergy().
     * Родитель теряет столько же.
     *
     * Потомок помещается в список newborns,
     * поэтому не делает ход в середине
     * текущего шага.
     */
    protected void tryReproduce() {

        if (isDead()) {
            return;
        }

        /*
         * До порога энергии не дошли.
         */
        if (getEnergy() < getReproductionThreshold()) {
            return;
        }

        /*
         * Ищем свободного случайного соседа.
         */
        int[] cell = env.findRandomFreeNeighbor(getX(), getY());

        if (cell == null) {
            return;
        }

        /*
         * Энергия потомка.
         */
        int childEnergy = getOffspringEnergy();

        /*
         * Теоретически сюда уже должны
         * попадать только способные к делению
         * агенты, но оставляем защитную проверку.
         */
        if (getEnergy() < childEnergy) {
            return;
        }

        /*
         * Создаём потомка.
         */
        Agent child = createOffspring(cell[0], cell[1]);

        /*
         * Родитель отдаёт ребёнку его энергию.
         */
        changeEnergy(-childEnergy);

        /*
         * Потомок появится в основном списке
         * только после завершения текущего хода.
         */
        env.addNewborn(child, cell[0], cell[1]);
    }

    /*
     * ==========================================
     * АБСТРАКТНЫЙ ИНТЕРФЕЙС
     * ==========================================
     */

    public abstract int getMaxEnergy();

    protected abstract int getTurnEnergyCost();

    protected abstract int getReproductionThreshold();

    protected abstract int getOffspringEnergy();

    public abstract void act();

    protected abstract Agent createOffspring(int x, int y);
}