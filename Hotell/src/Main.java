import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.Iterator;

abstract class Rom {
    final int romnummer, etasje, kvadrat, ant_senger;
    boolean erLedig = true;
    Rom neste = null;
    Rom nesteRomEtasje = null;
    public Rom(int romnummer, int etasje, int kvadrat, int ant_senger) {
        this.romnummer = romnummer;
        this.etasje = etasje;
        this.kvadrat = kvadrat;
        this.ant_senger = ant_senger;
    }
    public boolean erEgnet(int ant_seng, boolean kjokken) {
        if (!erLedig) return false;
        if (ant_senger != ant_seng) return false;
        if (kjokken) return false;
        return true;
    }
    @Override
    public String toString() {
        return romnummer + "med etaasje: " + etasje + " kvadrat " + kvadrat + " sengeplasser: " + ant_senger + " er ledig: " +erLedig;
    }
}
class EnkelRom extends Rom {
    public EnkelRom(int romnummer, int etasje, int kvadrat, int ant_senger) {
        super(romnummer, etasje, kvadrat, ant_senger);
    }
    @Override
    public String toString() {
        return "EnekEL Rom" + romnummer + "med etaasje: " + etasje + " kvadrat " + kvadrat + " sengeplasser: " + ant_senger + " er ledig: " +erLedig;
    }
}
class VanligRom extends Rom {
    public VanligRom(int romnummer, int etasje, int kvadrat, int ant_senger) {
        super(romnummer, etasje, kvadrat, ant_senger);
    }
    @Override
    public String toString() {
        return "Vanlig Rom" + romnummer + "med etaasje: " + etasje + " kvadrat " + kvadrat + " sengeplasser: " + ant_senger + " er ledig: " +erLedig;
    }
}
class Suite extends Rom {
    public Suite(int romnummer, int etasje, int kvadrat, int ant_senger) {
        super(romnummer, etasje, kvadrat, ant_senger);
    }
    @Override
    public String toString() {
        return "Suite " + romnummer + "med etaasje: " + etasje + " kvadrat " + kvadrat + " sengeplasser: " + ant_senger + " er ledig: " +erLedig;
    }
}
interface Kjokken {
    int hentkjokkenstorrelse();
}
class VanligeRomMedKjokken extends VanligRom implements Kjokken {
    final int kjokken_kvd;
    public VanligeRomMedKjokken(int romnummer, int etasje, int kvadrat, int ant_senger, int kjokken_kvd) {
        super(romnummer, etasje, kvadrat, ant_senger);
        this.kjokken_kvd = kjokken_kvd;
    }
    @Override
    public int hentkjokkenstorrelse()  {
        return kjokken_kvd;
    }
    @Override
    public boolean erEgnet(int ant_senger, boolean kjokken) {
        if (!erLedig) return false;
        if (ant_senger != this.ant_senger) return false;
        return true;
    }
    @Override
    public String toString() {
        return super.toString() + " med " + kjokken_kvd + " m2 kjokken";
    }
}
class SuiteMedKjokken extends Suite implements Kjokken {
    final int kjokken_kvd;
    public SuiteMedKjokken(int romnummer, int etasje, int kvadrat, int ant_senger, int kjokken_kvd) {
        super(romnummer, etasje, kvadrat, ant_senger);
        this.kjokken_kvd = kjokken_kvd;
    }
    @Override
    public int hentkjokkenstorrelse() {
        return kjokken_kvd;
    }
    @Override
    public boolean erEgnet(int ant_senger, boolean kjokken) {
        if (this.ant_senger != ant_senger) return false;
        if (!erLedig) return false;
        return true;
    }
    @Override
    public String toString() {
        return super.toString() + " med " + kjokken_kvd + " m2 kjokken";
    }
}
class Hotell implements Iterable<Rom> {
    final int MAX_ANT_SENGEPLASSER = 8;
    Rom forsteRom = null;
    Reservasjon forsteR, sisteR;
    final int ANTALL_ETASJER;
    Rom[] forsteRomEtasje;
    public Hotell(int antall_etasjer) {
        ANTALL_ETASJER = antall_etasjer;
        forsteRomEtasje = new Rom[ANTALL_ETASJER + 1];
    }
    private class RomIterator implements Iterator<Rom> {
        int denneEtasje;
        Rom denneRom;
        public RomIterator() {
            denneEtasje = 0;
            denneRom = forsteRomEtasje[0];
            finnEtRom();
        }
        public void finnEtRom() {
            while(denneEtasje <= ANTALL_ETASJER && denneRom == null) {
                denneEtasje++;
                if (denneEtasje <= ANTALL_ETASJER) {
                    denneRom = forsteRomEtasje[denneEtasje];
                }
            }
        }
        @Override
        public Rom next() {
            Rom svar = denneRom;
            denneRom = denneRom.nesteRomEtasje;
            finnEtRom();
            return svar;
        }
        @Override
        public boolean hasNext() {
            return denneEtasje <= ANTALL_ETASJER;
        }
    }
    public RomIterator iterator()  {
        return new RomIterator();
    }
    public int[] ledigeRom() {
        int[] ledige = new int[MAX_ANT_SENGEPLASSER];
        for (Rom r : this) {
            if (r.erLedig) {
                ledige[r.ant_senger-1]++;
            }
        }
        return ledige;
    }
    public Reservasjon finnRes(String navn) {
        Reservasjon p = forsteR;
        while (p != null) {
            if (p.gjest.navn.equals(navn)) {
                return p;
            }
            p = p.nesteR;
        }
        return null;
    }
    void tildelRom(String navn) {
        Reservasjon res = finnRes(navn);
        if (res == null) {
            throw new IngenReservasjon(navn);
        }
        taUtRes(res);
        Rom rom = null;
        for (int sx = res.onskeSenger; sx <= MAX_ANT_SENGEPLASSER; sx++) {
            rom = finnRom(sx, res.onskeKjokken);
            if (rom != null) {
                break;
            }
        }
        if (rom == null) {
            throw new IkkeLedigRom(navn, res.onskeSenger);
        }
        rom.erLedig = false;
        res.gjest.rom = rom;
        rom.toString();
    }
    public Rom finnRom(int antSeng, boolean kjokken) {
        Rom romP = forsteRom;
        while (romP != null) {
            if (romP.erEgnet(antSeng, kjokken)) {
                return romP;
            }
            romP = romP.neste;
        }
        return null;
    }
    public void taUtRes(Reservasjon res) {
        if (res == forsteR && res == sisteR) {
            forsteR = sisteR = null;
        } else if (res == forsteR) {
            forsteR = forsteR.nesteR;
            forsteR.forrigeR = null;
        } else if (res == sisteR) {
            sisteR = sisteR.forrigeR;
            sisteR.nesteR = null;
        } else {
            res.forrigeR.nesteR = res.nesteR;
            res.forrigeR.nesteR = res.forrigeR;
        }
        res.forrigeR = res.nesteR = null;
    }
}
class Gjest {
    String navn;
    Rom rom = null;
}
class Reservasjon {
    Gjest gjest;
    int onskeSenger;
    boolean onskeKjokken;
    Reservasjon forrigeR;
    Reservasjon nesteR;
}
class IngenReservasjon extends RuntimeException {
    IngenReservasjon (String navn) {
        super(navn + " har ingen reservasjon");
    }
}
class IkkeLedigRom extends RuntimeException {
    IkkeLedigRom(String navn, int senger) {
        super("vi har ikke et rom med " + senger + " navn");
    }
}
class Hotellkjede {
    int ANTALL_HOTELLER =2;
    int KJEDE_MAKS_ANTALL_SENGEPLASSER = 10;
    Hotell[] alleHoteller = new Hotell[ANTALL_HOTELLER];
    public void skrivUtLedigeRomMedTrader() {
        Monitor monitor = new Monitor(KJEDE_MAKS_ANTALL_SENGEPLASSER);
        CountDownLatch teller = new CountDownLatch(ANTALL_HOTELLER);
        for (Hotell h : alleHoteller) {
            new Thread(new Romteller(h, monitor, teller)).start();
        } try {
            teller.await();
        } catch (InterruptedException e) {
        }
        System.out.println("Totalt antall ledige rom:");
        int[] ledige = monitor.hentLedigeRom();
        for (int i = 0;  i < ledige.length;  ++i)
            System.out.printf("%4d sengeplasser: %6d\n", i+1, ledige[i]);
    }
}
class Romteller implements Runnable {
    Hotell hotell;
    Monitor mon;
    CountDownLatch teller;
    public Romteller(Hotell h, Monitor m, CountDownLatch t) {
        hotell = h;
        mon = m;
        teller = t;
    }
    @Override
    public void run() {
        mon.rapporterLedigeRom(hotell.ledigeRom());
        teller.countDown();
    }
}
class Monitor {
    Lock las = new ReentrantLock();
    int[] sumledige;

    public Monitor(int maxseng) {
        sumledige = new int[maxseng];
    }
    public void rapporterLedigeRom(int[] ledige) {
        las.lock();
        for (int i = 0; i < ledige.length; i++) {
            sumledige[i] += ledige[i];
        }
        las.unlock();
    }
    public int[] hentLedigeRom() {
        return sumledige;
    }
}

