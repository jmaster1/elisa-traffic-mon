package jmaster.etm.server.model.report;

import jmaster.etm.server.model.PhoneOwner;

import java.util.List;

public final class ConsumptionReportColors {

    private static final List<String> COLORS = List.of(
            "#2563eb",
            "#dc2626",
            "#16a34a",
            "#9333ea",
            "#ea580c",
            "#0891b2",
            "#be123c",
            "#4f46e5"
    );

    private ConsumptionReportColors() {
    }

    public static String forPhone(Long phoneNr) {
        if (phoneNr == null) {
            return COLORS.get(0);
        }
        PhoneOwner owner = PhoneOwner.fromPhone(phoneNr);
        int index = owner == null ?
                Math.floorMod(Long.hashCode(phoneNr), COLORS.size()) :
                owner.ordinal() % COLORS.size();
        return COLORS.get(index);
    }
}
