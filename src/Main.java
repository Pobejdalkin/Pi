import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Scanner;

public class Main {

    private static final String PI_API_URL = "https://api.pi.delivery/v1/pi?start=%d&numberOfDigits=%d";
    private static final int CHUNK_SIZE = 1000; // Размер загружаемого блока цифр
    private static final int CONTEXT_DIGITS = 25; // Количество цифр для отображения вокруг найденной
    private static final long MAX_POSITION = 1_000_000_000; // Максимальная позиция для поиска

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите последовательность для поиска в π: ");
        String sequence = scanner.nextLine().trim();

        if (!sequence.matches("\\d+")) {
            System.out.println("Ошибка: последовательность должна содержать только цифры");
            return;
        }

        try {
            long position = findSequenceInPi(sequence);
            if (position != -1) {
                System.out.printf("Последовательность '%s' найдена на позиции %d%n", sequence, position);
                printContext(position, sequence.length());
            } else {
                System.out.printf("Последовательность '%s' не найдена в первых %,d цифрах π%n", sequence, MAX_POSITION);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при поиске: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static long findSequenceInPi(String sequence) throws Exception {
        long currentPosition = 0;
        String overlap = ""; // Для хранения перекрытия между блоками

        while (currentPosition < MAX_POSITION) {
            int digitsToFetch = CHUNK_SIZE;

            // Уменьшаем количество запрашиваемых цифр в конце
            if (currentPosition + digitsToFetch > MAX_POSITION) {
                digitsToFetch = (int)(MAX_POSITION - currentPosition);
            }

            String piChunk = fetchPiDigits(currentPosition, digitsToFetch);
            String searchArea = overlap + piChunk;

            int indexInSearchArea = searchArea.indexOf(sequence);

            if (indexInSearchArea != -1) {
                return currentPosition - overlap.length() + indexInSearchArea;
            }

            // Сохраняем конец блока для перекрытия со следующим
            overlap = searchArea.substring(Math.max(0, searchArea.length() - sequence.length() + 1));

            currentPosition += piChunk.length();
            System.out.println(currentPosition);

            // Прогресс-бар
            if (currentPosition % 1_000_000 == 0) {
                System.out.printf("Проверено: %,d позиций%n", currentPosition);
            }
        }

        return -1;
    }

    private static String fetchPiDigits(long start, int numberOfDigits) throws Exception {
        String url = String.format(PI_API_URL, start, numberOfDigits);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            String line;
            StringBuilder jsonResponse = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                jsonResponse.append(line);
            }
            // Извлекаем цифры из JSON ответа {"content":"1415926535..."}
            String response = jsonResponse.toString();
            return response.split("\"content\":\"")[1].split("\"")[0];
        }
    }

    private static void printContext(long position, int sequenceLength) throws Exception {
        long startPos = Math.max(0, position - CONTEXT_DIGITS);
        int digitsToFetch = CONTEXT_DIGITS + sequenceLength + CONTEXT_DIGITS;
        String context = fetchPiDigits(startPos, digitsToFetch);

        System.out.println("Окружающие цифры:");
        System.out.println(context.substring(0, CONTEXT_DIGITS) +
                "[" + context.substring(CONTEXT_DIGITS, CONTEXT_DIGITS + sequenceLength) + "]" +
                context.substring(CONTEXT_DIGITS + sequenceLength));
    }
}