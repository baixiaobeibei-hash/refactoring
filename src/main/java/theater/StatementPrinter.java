package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import static theater.Constants.*;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private final Invoice invoice;
    private final Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        int totalAmount = 0;
        int volumeCredits = 0;
        StringBuilder result = new StringBuilder("Statement for " + invoice.getCustomer() + System.lineSeparator());

        NumberFormat frmt = NumberFormat.getCurrencyInstance(Locale.US);

        for (Performance p : invoice.getPerformances()) {
            Play play = plays.get(p.playID);

            // add volume credits
            volumeCredits += getVolumeCredits(p, play);

            // print line for this order
            result.append(String.format("  %s: %s (%s seats)%n", play.name, frmt.format(getAmount(p, play) / PERCENT_FACTOR), p.audience));
            totalAmount += getAmount(p, play);
        }
        result.append(String.format("Amount owed is %s%n", frmt.format(totalAmount / PERCENT_FACTOR)));
        result.append(String.format("You earned %s credits%n", volumeCredits));
        return result.toString();
    }

    private static int getVolumeCredits(Performance p, Play play) {
        int volumeCredits = Math.max(p.audience - BASE_VOLUME_CREDIT_THRESHOLD, 0);
        // add extra credit for every five comedy attendees
        if ("comedy".equals(play.type)) volumeCredits += p.audience / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        return volumeCredits;
    }

    private static int getAmount(Performance p, Play play) {
        int thisAmount = 0;
        switch (play.type) {
            case "tragedy":
                thisAmount = TRAGEDY_BASE_AMOUNT;
                if (p.audience > TRAGEDY_AUDIENCE_THRESHOLD) {
                    thisAmount += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON * (p.audience - TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case "comedy":
                thisAmount = Constants.COMEDY_BASE_AMOUNT;
                if (p.audience > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    thisAmount += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (p.audience - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                thisAmount += Constants.COMEDY_AMOUNT_PER_AUDIENCE * p.audience;
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", play.type));
        }
        return thisAmount;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public Map<String, Play> getPlays() {
        return plays;
    }
}
