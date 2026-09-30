public class SimulationConfig {

    /*
     * ==========================================
     * ЗНАЧЕНИЯ ПО УМОЛЧАНИЮ
     * ==========================================
     *
     * Эти значения будут сразу показаны
     * в стартовом окне.
     */

    public static final int DEFAULT_WIDTH = 50;

    public static final int DEFAULT_HEIGHT = 40;

    public static final int DEFAULT_PLANTS = 260;

    public static final int DEFAULT_PLANT_ENERGY = 4;

    public static final int DEFAULT_HERBIVORES = 50;

    public static final int DEFAULT_HERBIVORE_ENERGY = 30;

    /*
     * В консольной версии было 40,
     * но по твоему новому требованию
     * для оконной версии должно быть 35.
     */
    public static final int DEFAULT_PREDATORS = 35;

    /*
     * Энергия хищника по умолчанию
     * остаётся той, что задана
     * в самой логике консольной версии.
     */
    public static final int DEFAULT_PREDATOR_ENERGY = Predator.INITIAL_ENERGY;

    /*
     * ==========================================
     * ПОЛЯ КОНФИГУРАЦИИ
     * ==========================================
     */

    private final int width;
    private final int height;

    private final int plantCount;
    private final int plantInitialEnergy;

    private final int herbivoreCount;
    private final int herbivoreInitialEnergy;

    private final int predatorCount;
    private final int predatorInitialEnergy;

    /*
     * ==========================================
     * КОНСТРУКТОР ПО УМОЛЧАНИЮ
     * ==========================================
     */

    public SimulationConfig() {

        this(DEFAULT_WIDTH, DEFAULT_HEIGHT,

                DEFAULT_PLANTS, DEFAULT_PLANT_ENERGY,

                DEFAULT_HERBIVORES, DEFAULT_HERBIVORE_ENERGY,

                DEFAULT_PREDATORS, DEFAULT_PREDATOR_ENERGY);
    }

    /*
     * ==========================================
     * ОСНОВНОЙ КОНСТРУКТОР
     * ==========================================
     */

    public SimulationConfig(int width, int height,

                            int plantCount, int plantInitialEnergy,

                            int herbivoreCount, int herbivoreInitialEnergy,

                            int predatorCount, int predatorInitialEnergy) {

        validate(width, height,

                plantCount, plantInitialEnergy,

                herbivoreCount, herbivoreInitialEnergy,

                predatorCount, predatorInitialEnergy);

        this.width = width;
        this.height = height;

        this.plantCount = plantCount;
        this.plantInitialEnergy = plantInitialEnergy;

        this.herbivoreCount = herbivoreCount;

        this.herbivoreInitialEnergy = herbivoreInitialEnergy;

        this.predatorCount = predatorCount;

        this.predatorInitialEnergy = predatorInitialEnergy;
    }

    /*
     * ==========================================
     * ПРОВЕРКА ДАННЫХ
     * ==========================================
     */

    private static void validate(int width, int height,

                                 int plantCount, int plantInitialEnergy,

                                 int herbivoreCount, int herbivoreInitialEnergy,

                                 int predatorCount, int predatorInitialEnergy) {

        if (width <= 0) {

            throw new IllegalArgumentException("Ширина поля должна быть больше 0.");
        }

        if (height <= 0) {

            throw new IllegalArgumentException("Высота поля должна быть больше 0.");
        }

        if (plantCount < 0) {

            throw new IllegalArgumentException("Количество растений не может быть отрицательным.");
        }

        if (herbivoreCount < 0) {

            throw new IllegalArgumentException("Количество травоядных не может быть отрицательным.");
        }

        if (predatorCount < 0) {

            throw new IllegalArgumentException("Количество хищников не может быть отрицательным.");
        }

        if (plantInitialEnergy < 0) {

            throw new IllegalArgumentException("Начальная энергия растения не может быть отрицательной.");
        }

        if (herbivoreInitialEnergy < 0) {

            throw new IllegalArgumentException("Начальная энергия травоядного не может быть отрицательной.");
        }

        if (predatorInitialEnergy < 0) {

            throw new IllegalArgumentException("Начальная энергия хищника не может быть отрицательной.");
        }

        /*
         * Важно:
         *
         * Здесь специально НЕ проверяется,
         * что общее число начальных агентов
         * <= width * height.
         *
         * Само размещение будет выполнять
         * отдельный класс симуляции.
         */
    }

    /*
     * ==========================================
     * GETTERS
     * ==========================================
     */

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getPlantCount() {
        return plantCount;
    }

    public int getPlantInitialEnergy() {
        return plantInitialEnergy;
    }

    public int getHerbivoreCount() {
        return herbivoreCount;
    }

    public int getHerbivoreInitialEnergy() {
        return herbivoreInitialEnergy;
    }

    public int getPredatorCount() {
        return predatorCount;
    }

    public int getPredatorInitialEnergy() {
        return predatorInitialEnergy;
    }

    /*
     * ==========================================
     * УДОБНЫЕ ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ
     * ==========================================
     */

    public int getTotalInitialAgents() {

        return plantCount + herbivoreCount + predatorCount;
    }

    public int getFieldSize() {

        return width * height;
    }

    /*
     * Проверяем, может ли вообще
     * начальная популяция разместиться
     * на поле.
     */
    public boolean fitsOnField() {

        return getTotalInitialAgents() <= getFieldSize();
    }

    @Override
    public String toString() {

        return "SimulationConfig{" +

                "width=" + width +

                ", height=" + height +

                ", plantCount=" + plantCount +

                ", plantInitialEnergy=" + plantInitialEnergy +

                ", herbivoreCount=" + herbivoreCount +

                ", herbivoreInitialEnergy=" + herbivoreInitialEnergy +

                ", predatorCount=" + predatorCount +

                ", predatorInitialEnergy=" + predatorInitialEnergy +

                '}';
    }
}