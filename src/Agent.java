public abstract class Agent {

    private int x;
    private int y;
    private int energy;

    private final char symbol;

    protected final Environment env;

    protected Agent(Environment env, int x, int y, int energy, char symbol) {
        if (env == null) {
            throw new IllegalArgumentException("Среда не может быть null.");
        }

        if (!env.isInside(x, y)) {
            throw new IllegalArgumentException(
                    "Координаты агента находятся за пределами поля."
            );
        }

        this.env = env;

        this.x = x;
        this.y = y;

        this.energy = energy;
        this.symbol = symbol;
    }

    // GETTERS

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

    // ПОЗИЦИЯ

    public void setPosition(int x, int y) {
        if (!env.isInside(x, y)) {
            throw new IllegalArgumentException(
                    "Нельзя установить позицию за пределами поля."
            );
        }

        this.x = x;
        this.y = y;
    }

    // ЭНЕРГИЯ

    public void setEnergy(int energy) {
        this.energy = Math.max(
                0,
                Math.min(energy, getMaxEnergy())
        );
    }

    public void changeEnergy(int delta) {
        setEnergy(energy + delta);
    }

    public boolean isDead() {
        return energy <= 0;
    }

    // НАЧАЛО ХОДА

    protected boolean beginTurn() {

        if (isDead()) {
            env.markDead(this);
            return false;
        }

        // Расход энергии за сам ход.
        changeEnergy(-getTurnEnergyCost());

        if (isDead()) {
            env.markDead(this);
            return false;
        }

        return true;
    }

    // РАЗМНОЖЕНИЕ

    protected void tryReproduce() {

        if (isDead()) {
            return;
        }

        /*
         * Размножение теперь не зависит от случайной вероятности.
         *
         * Если агент достиг порога энергии,
         * то он пытается размножиться.
         */
        if (getEnergy() < getReproductionThreshold()) {
            return;
        }

        int[] cell = env.findRandomFreeNeighbor(
                getX(),
                getY()
        );

        if (cell == null) {
            return;
        }

        int childEnergy = getOffspringEnergy();

        if (getEnergy() < childEnergy) {
            return;
        }

        Agent child = createOffspring(
                cell[0],
                cell[1]
        );

        // Родитель отдаёт ребёнку его энергию.
        changeEnergy(-childEnergy);

        env.addNewborn(
                child,
                cell[0],
                cell[1]
        );
    }

    // АБСТРАКТНЫЙ ИНТЕРФЕЙС

    public abstract int getMaxEnergy();

    protected abstract int getTurnEnergyCost();

    protected abstract int getReproductionThreshold();

    protected abstract int getOffspringEnergy();

    public abstract void act();

    protected abstract Agent createOffspring(int x, int y);
}
