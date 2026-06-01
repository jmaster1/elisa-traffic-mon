package jmaster.etm.server.model;

public enum PhoneOwner {
    Anna(56206977),
    Artemi(53065326),
    Jegor(56876853),
    Paasiku(56971710),
    Timur(5133984),
    Valeri(58877126);

    public final long phoneNr;

    PhoneOwner(long phoneNr) {
        this.phoneNr = phoneNr;
    }

    public static PhoneOwner fromPhone(long phoneNr) {
        for(PhoneOwner e : values()) {
            if(e.phoneNr == phoneNr) {
                return e;
            }
        }
        return null;
    }
}
