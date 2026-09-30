public class Plant extends Agent {

    public static final int INITIAL_ENERGY = 4;
    public static final int MAX_ENERGY = 14;

    private static final int GROWTH_PER_TURN = 1;
    private static final int REPRODUCTION_THRESHOLD = 12;
    private static final int OFFSPRING_ENERGY = 4;

    public Plant(Environment env, int x, int y) {
        super(env, x, y, INITIAL_ENERGY, 'Р');
    }

    @Override
    public int getMaxEnergy() {
        return MAX_ENERGY;
    }

    @Override
    protected int getTurnEnergyCost() {
        return 0;
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

        if (isDead()) {
            env.markDead(this);
            return;
        }

        changeEnergy(GROWTH_PER_TURN);

        tryReproduce();
    }

    @Override
    protected Agent createOffspring(int x, int y) {
        return new Plant(env, x, y);
    }
}