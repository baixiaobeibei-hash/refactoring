package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private final Invoice invoice;
    private final Map<String, Play> plays;

    /**
     * Constructs a StatementPrinter with the specified invoice and plays.
     *
     * @param invoice the invoice to generate a statement for
     * @param plays   the map of play IDs to Play objects
     */
    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     *
     * @return the formatted statement string
     * @throws RuntimeException if the play type is unknown
     */
    public String statement() {
        final StringBuilder result = new StringBuilder(
                "Statement for " + invoice.getCustomer() + System.lineSeparator()
        );
        final NumberFormat from = NumberFormat.getCurrencyInstance(Locale.US);

        for (Performance performance : invoice.getPerformances()) {
            final Play play = getPlay(performance);
            result.append(String.format("  %s: %s (%s seats)%n",
                    play.getName(),
                    usd(performance, from, play),
                    performance.getAudience()));
        }

        result.append(String.format("Amount owed is %s%n",
                from.format(getTotalAmount() / Constants.PERCENT_FACTOR)));
        result.append(String.format("You earned %s credits%n", getTotalVolumeCredits()));

        return result.toString();
    }

    private String usd(Performance performance, NumberFormat from, Play play) {
        return from.format(getAmount(performance, play) / Constants.PERCENT_FACTOR);
    }

    private int getTotalVolumeCredits() {
        int result = 0;
        for (Performance performance : invoice.getPerformances()) {
            final Play play = getPlay(performance);
            result += getVolumeCredits(performance, play);
        }
        return result;
    }

    private Play getPlay(Performance performance) {
        return plays.get(performance.getPlayID());
    }

    private int getTotalAmount() {
        int result = 0;
        for (Performance performance : invoice.getPerformances()) {
            final Play play = getPlay(performance);
            result += getAmount(performance, play);
        }
        return result;
    }

    private static int getVolumeCredits(Performance performance, Play play) {
        int result = Math.max(performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
        if ("comedy".equals(play.getType())) {
            result += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return result;
    }

    private static int getAmount(Performance performance, Play play) {
        int result = 0;
        switch (play.getType()) {
            case "tragedy":
                result = Constants.TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", play.getType()));
        }
        return result;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public Map<String, Play> getPlays() {
        return plays;
    }
}
