public class Plant extends Agent {

    /*
     * ==========================================
     * ЭНЕРГИЯ
     * ==========================================
     */

    /*
     * Стандартная начальная энергия
     * из консольной версии.
     */
    public static final int INITIAL_ENERGY = 4;

    /*
     * Максимальная энергия растения.
     */
    public static final int MAX_ENERGY = 14;

    /*
     * ==========================================
     * РОСТ
     * ==========================================
     */

    /*
     * Каждый ход растение
     * получает +1 энергию.
     */
    private static final int GROWTH_PER_TURN = 1;

    /*
     * После достижения 12 энергии
     * растение пытается размножиться.
     */
    private static final int REPRODUCTION_THRESHOLD = 12;

    /*
     * Ребёнку передаётся 4 энергии.
     */
    private static final int OFFSPRING_ENERGY = 4;

    /*
     * ==========================================
     * КОНСТРУКТОРЫ
     * ==========================================
     */

    /*
     * Конструктор по умолчанию.
     *
     * Нужен для логики, соответствующей
     * консольной версии.
     */
    public Plant(Environment env, int x, int y) {

        this(env, x, y, INITIAL_ENERGY);
    }

    /*
     * Конструктор с заданной
     * начальной энергией.
     *
     * Он нужен оконному приложению,
     * потому что пользователь сможет
     * указать стартовую энергию растения
     * в окне настроек.
     */
    public Plant(Environment env, int x, int y, int initialEnergy) {

        super(env, x, y, initialEnergy, 'Р');
    }

    /*
     * ==========================================
     * ЭНЕРГИЯ
     * ==========================================
     */

    @Override
    public int getMaxEnergy() {
        return MAX_ENERGY;
    }

    /*
     * Растение не тратит энергию
     * на собственный ход.
     */
    @Override
    protected int getTurnEnergyCost() {
        return 0;
    }

    /*
     * Порог размножения.
     */
    @Override
    protected int getReproductionThreshold() {
        return REPRODUCTION_THRESHOLD;
    }

    /*
     * Энергия ребёнка.
     */
    @Override
    protected int getOffspringEnergy() {
        return OFFSPRING_ENERGY;
    }

    /*
     * ==========================================
     * EMOJI
     * ==========================================
     */

    @Override
    public String getEmoji() {
        return "🌿";
    }

    /*
     * ==========================================
     * ДЕЙСТВИЕ
     * ==========================================
     *
     * Растение:
     *
     * 1. Не двигается.
     * 2. Получает +1 энергии.
     * 3. Проверяет возможность размножения.
     */

    @Override
    public void act() {

        /*
         * Защита от действия
         * уже погибшего растения.
         */
        if (isDead()) {

            env.markDead(this);

            return;
        }

        /*
         * Рост растения.
         */
        changeEnergy(GROWTH_PER_TURN);

        /*
         * После роста проверяем
         * возможность размножения.
         */
        tryReproduce();
    }

    /*
     * ==========================================
     * РОЖДЕНИЕ
     * ==========================================
     */

    @Override
    protected Agent createOffspring(int x, int y) {

        /*
         * Потомок растения получает
         * стандартные 4 единицы энергии,
         * после чего родитель отдаёт ему
         * 4 единицы через tryReproduce().
         */
        return new Plant(env, x, y);
    }
}