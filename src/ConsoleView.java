public class ConsoleView {

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";

    private final Environment env;

    public ConsoleView(Environment env) {
        this.env = env;
    }

    public void printGrid() {

        System.out.println("Шаг: " + env.getStep());

        System.out.println("Всего сущностей: " + env.getAgentCount());

        System.out.println(
                "Растений: " + env.getPlantCount()
                        + " | Травоядных: " + env.getHerbivoreCount()
                        + " | Хищников: " + env.getPredatorCount()
        );

        System.out.println();

        for (int y = 0; y < env.getHeight(); y++) {

            for (int x = 0; x < env.getWidth(); x++) {

                Agent agent = env.getAgent(x, y);

                if (agent == null) {
                    System.out.print(". ");
                    continue;
                }

                if (agent instanceof Predator) {
                    System.out.print(RED + "Х" + RESET + " ");
                } else if (agent instanceof Herbivore) {
                    System.out.print(BLUE + "Т" + RESET + " ");
                } else if (agent instanceof Plant) {
                    System.out.print(GREEN + "Р" + RESET + " ");
                } else {
                    System.out.print(agent.getSymbol() + " ");
                }
            }

            System.out.println();
        }
    }

    public void printStatistics() {

        System.out.println();

        System.out.println("ИТОГОВАЯ СТАТИСТИКА");

        System.out.println();

        System.out.println(
                "Продолжительность: "
                        + env.getStep()
                        + " шагов"
        );

        System.out.println(
                "Растений: "
                        + env.getPlantCount()
        );

        System.out.println(
                "Травоядных: "
                        + env.getHerbivoreCount()
        );

        System.out.println(
                "Хищников: "
                        + env.getPredatorCount()
        );

        System.out.println(
                "Всего сущностей: "
                        + env.getAgentCount()
        );
    }
}
