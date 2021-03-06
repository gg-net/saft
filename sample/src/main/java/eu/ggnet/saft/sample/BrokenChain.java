package eu.ggnet.saft.sample;

import java.util.Optional;

/**
 * Shows a chain,that is broken through a null result.
 *
 * @author oliver.guenther
 */
public class BrokenChain {

    public static void main(String[] args) {
        optionalSteam();
    }

    public static void optionalSteam() {

        Optional.of(true).map(v -> {
            System.out.println("One");
            return "X";
        }).map(v -> {
            System.out.println("Two");
            return "Y";
        }).map(v -> {
            System.out.println("Three");
            return null; // Return null, breaks the chain/stream
        }).map(v -> {
            System.out.println("Will never be shown");
            return "X";
        }).ifPresent(v -> System.out.print("Ende"));

    }

}
