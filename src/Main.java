import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.locks.LockSupport;

public class Main {

    private static final int WIDTH = 50;
    private static final int HEIGHT = 40;

    private static final int INITIAL_PLANTS = 260;
    private static final int INITIAL_HERBIVORES = 50;
    private static final int INITIAL_PREDATORS = 35;

    private static final int FAST_FORWARD_STEPS = 10;

    /*
     * ЗАДЕРЖКА МЕЖДУ ШАГАМИ АВТОМАТИЧЕСКОГО РЕЖИМА.
     *
     * 0.2 миллисекунды = 200 000 наносекунд.
     *
     * Можно менять вручную:
     *
     * 0.5 мс -> 500_000
     * 1 мс   -> 1_000_000
     * 5 мс   -> 5_000_000
     * 10 мс  -> 10_000_000
     *
     * Значение 0 отключает искусственную задержку.
     */
    private static final double AUTO_STEP_DELAY_MS = 0.2;

    /*
     * Как часто обновлять изображение поля
     * во время автоматического режима.
     *
     * Само моделирование идёт быстрее,
     * а консоль обновляется отдельно.
     *
     * Например:
     * 50  -> очень часто
     * 100 -> комфортное обновление
     * 250 -> более редкое обновление
     */
    private static final long AUTO_RENDER_INTERVAL_MS = 100;

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        Random random = new Random();

        Environment env = new Environment(WIDTH, HEIGHT);

        ConsoleView view = new ConsoleView(env);

        // Начальный спавн.
        spawn(env, random, INITIAL_PLANTS, 0);
        spawn(env, random, INITIAL_HERBIVORES, 1);
        spawn(env, random, INITIAL_PREDATORS, 2);

        while (true) {

            clearConsole();

            view.printGrid();

            System.out.println();
            System.out.println("Enter -> следующий шаг");
            System.out.println("h + Enter -> выполнить 10 шагов");
            System.out.println("j + Enter -> автоматический режим до вымирания вида");
            System.out.println("q + Enter -> завершить программу");

            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("q")) {
                break;
            }

            /*
             * АВТОМАТИЧЕСКИЙ РЕЖИМ
             */
            if (input.equalsIgnoreCase("j")) {

                runAutomaticMode(env, view);

                System.out.println();
                System.out.println("Нажмите Enter для завершения программы.");

                scanner.nextLine();
                break;
            }

            /*
             * Выполнить 10 шагов.
             */
            if (input.equalsIgnoreCase("h")) {

                for (int i = 0; i < FAST_FORWARD_STEPS; i++) {

                    if (simulationFinished(env)) {
                        break;
                    }

                    env.step();
                }

            } else {

                /*
                 * Обычный одиночный шаг.
                 */
                if (!simulationFinished(env)) {
                    env.step();
                }
            }
        }

        clearConsole();

        view.printGrid();

        view.printStatistics();

        scanner.close();
    }

    /**
     * Автоматический режим.
     *
     * Симуляция идёт до тех пор,
     * пока все три типа существ присутствуют.
     *
     * Как только хотя бы один тип исчезает,
     * цикл останавливается.
     */
    private static void runAutomaticMode(
            Environment env,
            ConsoleView view
    ) {

        System.out.println();
        System.out.println("АВТОМАТИЧЕСКИЙ РЕЖИМ ЗАПУЩЕН");
        System.out.println(
                "Скорость: примерно "
                        + AUTO_STEP_DELAY_MS
                        + " мс между шагами."
        );
        System.out.println();

        final long delayNanos =
                (long) (AUTO_STEP_DELAY_MS * 1_000_000.0);

        long lastRenderTime = 0;

        while (!simulationFinished(env)) {

            /*
             * Один полноценный шаг симуляции.
             */
            env.step();

            /*
             * Небольшая пауза между шагами.
             *
             * LockSupport.parkNanos используется вместо
             * Thread.sleep(), потому что Thread.sleep()
             * работает в миллисекундах и не позволяет
             * нормально задать 0.2 мс.
             */
            if (delayNanos > 0) {
                LockSupport.parkNanos(delayNanos);
            }

            /*
             * Консоль не перерисовываем после каждого шага.
             *
             * При 0.2 мс это могло бы быть до ~5000 шагов/сек,
             * что привело бы к огромному количеству операций
             * вывода и фактически замедлило бы программу.
             */
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastRenderTime
                    >= AUTO_RENDER_INTERVAL_MS) {

                clearConsole();

                view.printGrid();

                System.out.println();
                System.out.println(
                        "АВТОМАТИЧЕСКИЙ РЕЖИМ"
                );

                System.out.println(
                        "Скорость шага: "
                                + AUTO_STEP_DELAY_MS
                                + " мс"
                );

                System.out.println(
                        "Симуляция продолжается..."
                );

                lastRenderTime = currentTime;
            }
        }

        /*
         * После завершения обязательно показываем
         * самое последнее состояние поля.
         */
        clearConsole();

        view.printGrid();

        System.out.println();
        System.out.println("АВТОМАТИЧЕСКИЙ РЕЖИМ ЗАВЕРШЁН");
        System.out.println();

        printEndReason(env);

        view.printStatistics();
    }

    /**
     * Проверяет, закончилась ли симуляция.
     *
     * Условие завершения:
     * хотя бы один из трёх видов полностью исчез.
     */
    private static boolean simulationFinished(
            Environment env
    ) {

        return env.getPlantCount() == 0
                || env.getHerbivoreCount() == 0
                || env.getPredatorCount() == 0;
    }

    /**
     * Выводит причину остановки автоматического режима.
     */
    private static void printEndReason(
            Environment env
    ) {

        boolean plantDead =
                env.getPlantCount() == 0;

        boolean herbivoreDead =
                env.getHerbivoreCount() == 0;

        boolean predatorDead =
                env.getPredatorCount() == 0;

        System.out.println(
                "Причина остановки:"
        );

        if (plantDead) {
            System.out.println(
                    "Растения: 0 — вид вымер."
            );
        }

        if (herbivoreDead) {
            System.out.println(
                    "Травоядные: 0 — вид вымер."
            );
        }

        if (predatorDead) {
            System.out.println(
                    "Хищники: 0 — вид вымер."
            );
        }
    }

    /**
     * Создание начальной популяции.
     */
    private static void spawn(
            Environment env,
            Random random,
            int amount,
            int type
    ) {

        int created = 0;

        int maxAttempts =
                env.getWidth()
                        * env.getHeight()
                        * 20;

        int attempts = 0;

        while (
                created < amount
                        && attempts++ < maxAttempts
        ) {

            int x =
                    random.nextInt(
                            env.getWidth()
                    );

            int y =
                    random.nextInt(
                            env.getHeight()
                    );

            if (env.getAgent(x, y) != null) {
                continue;
            }

            Agent agent;

            if (type == 0) {

                agent = new Plant(
                        env,
                        x,
                        y
                );

            } else if (type == 1) {

                agent = new Herbivore(
                        env,
                        x,
                        y
                );

            } else {

                agent = new Predator(
                        env,
                        x,
                        y
                );
            }

            if (env.addAgent(agent, x, y)) {
                created++;
            }
        }
    }

    /**
     * Очистка консоли.
     */
    private static void clearConsole() {

        System.out.print(
                "\033[H\033[2J"
        );

        System.out.flush();
    }
}

