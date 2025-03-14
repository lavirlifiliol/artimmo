package artimmo.game;

public record CombatSim(double pThreshold, double meMaxHp, double meCrit, double meFireRes, double meFireDpr,
                        double meWaterRes, double meWaterDpr, double meEarthRes, double meEarthDpr,
                        double meAirRes, double meAirDpr, double meRestore, double meBurn, double meLifesteal,
                        double meHealing, double foeMaxHp, double foeCrit, double foeFireRes, double foeFireDpr,
                        double foeWaterRes, double foeWaterDpr, double foeEarthRes, double foeEarthDpr,
                        double foeAirRes, double foeAirDpr, double foeRestore, double foeBurn,
                        double foeLifesteal, double foeHealing, double foeReconst, double foePoison) {
    public Results run() {
        double avgHealth = 0;
        double winRate = 0;
        double avgRounds = 0;
        double totalP = 0;

        double[] p = P.get();
        double[] meHp = ME_HP.get();
        double[] foeHp = FOE_HP.get();
        int[] round = ROUND.get();

        int i = 0;

        p[i] = 1;
        meHp[i] = meMaxHp;
        foeHp[i] = foeMaxHp;
        round[i] = 1;


        while (i >= 0) {
            Heap.heappop(i + 1, p, meHp, foeHp, round);
            if (totalP > pThreshold) {
                break;
            }
            if (foeHp[i] <= 0) {
                winRate += p[i];
                avgHealth += meHp[i] * p[i];
                avgRounds += round[i] * p[i];
                totalP += p[i];
                i -= 1;
            } else if (meHp[i] <= 0 || round[i] > 100) {
                totalP += p[i];
                avgRounds += round[i] * p[i];
                i -= 1;
            } else if (round[i] % 2 == 1) {
                // me attack
                i = meTurn(p, meHp, foeHp, round, i);
            } else {
                // foe attack
                i = foeTurn(p, meHp, foeHp, round, i);
            }
        }

        return new Results(avgHealth, winRate, avgRounds, totalP);
    }

    private int addOutcome(double[] p, double[] meHp, double[] foeHp, int[] round, double pN, double meHpN, double foeHpN, int roundN, int i) {
        if (pN < pThreshold / MAX_STACK / 10) {
            return i;
        }
        p[i] = pN;
        meHp[i] = meHpN;
        foeHp[i] = foeHpN;
        round[i] = roundN;
        Heap.heappush(i, p, meHp, foeHp, round);
        return i + 1;
    }

    private int addMyDamage(double[] p, double[] meHp, double[] foeHp, int[] round, double pN, double meHpN, double foeHpO, double damage, int roundN, int i) {
        i = addOutcome(p, meHp, foeHp, round, pN * (1 - meCrit / 100), meHpN, Math.round(foeHpO - damage), roundN, i);
        i = addOutcome(p, meHp, foeHp, round, pN * meCrit / 100, meHpN, Math.round(foeHpO - damage * 1.5), roundN, i);
        return i;
    }

    private double choose(boolean cond, double p) {
        return cond ? p : 1 - p;
    }

    private int foeTurn(double[] p, double[] meHp, double[] foeHp, int[] round, int i) {
        double pNow = p[i];
        double meHpNow = meHp[i];
        double foeHpNow = foeHp[i];
        int roundNow = round[i];
        double airBlock = Math.max(meAirRes / 1000, 0);
        double fireBlock = Math.max(meFireRes / 1000, 0);
        double waterBlock = Math.max(meWaterRes / 1000, 0);
        double earthBlock = Math.max(meEarthRes / 1000, 0);
        double airDmg = foeAirDpr - meAirRes * foeAirDpr / 100;
        double waterDmg = foeWaterDpr - meWaterRes * foeWaterDpr / 100;
        double earthDmg = foeEarthDpr - meEarthRes * foeEarthDpr / 100;
        double fireDmg = foeFireDpr - meFireRes * foeFireDpr / 100;

        if ((roundNow - 2) % 6 == 0) {
            meHpNow += foeHealing;
        }

        for (int blocks = 0; blocks < 16; ++blocks) {
            boolean didAirBlock = (blocks & 1) != 0;
            boolean didEarthBlock = (blocks & 2) != 0;
            boolean didFireBlock = (blocks & 4) != 0;
            boolean didWaterBlock = (blocks & 8) != 0;

            double pOutcome = pNow * choose(didAirBlock, airBlock)
                    * choose(didEarthBlock, earthBlock)
                    * choose(didFireBlock, fireBlock)
                    * choose(didWaterBlock, waterBlock);

            double damage = (didAirBlock ? 0 : airDmg)
                    + (didEarthBlock ? 0 : earthDmg)
                    + (didFireBlock ? 0 : fireDmg)
                    + (didWaterBlock ? 0 : waterDmg);
            i = addFoeDamage(p, meHp, foeHp, round, pOutcome, meHpNow, damage, foeHpNow, roundNow + 1, i);
        }

        return i - 1;
    }

    private int addFoeDamage(double[] p, double[] meHp, double[] foeHp, int[] round, double pN, double meHpNow, double damage, double foeHpN, int roundN, int i) {
        i = addOutcome(p, meHp, foeHp, round, pN * (1 - foeCrit / 100), Math.round(meHpNow - damage), foeHpN, roundN, i);
        i = addOutcome(p, meHp, foeHp, round, pN * foeCrit / 100, Math.round(meHpNow - damage * 1.5), foeHpN, roundN, i);
        return i;
    }

    private int meTurn(double[] p, double[] meHp, double[] foeHp, int[] round, int i) {
        double pNow = p[i];
        double meHpNow = meHp[i];
        double foeHpNow = foeHp[i];
        int roundNow = round[i];
        double airBlock = Math.max(foeAirRes / 1000, 0);
        double fireBlock = Math.max(foeFireRes / 1000, 0);
        double waterBlock = Math.max(foeWaterRes / 1000, 0);
        double earthBlock = Math.max(foeEarthRes / 1000, 0);
        double airDmg = meAirDpr - foeAirRes * meAirDpr / 100;
        double waterDmg = meWaterDpr - foeWaterRes * meWaterDpr / 100;
        double earthDmg = meEarthDpr - foeEarthRes * meEarthDpr / 100;
        double fireDmg = meFireDpr - foeFireRes * meFireDpr / 100;

        if ((roundNow - 1) % 6 == 0) {
            meHpNow += meHealing;
        }

        for (int blocks = 0; blocks < 16; ++blocks) {
            boolean didAirBlock = (blocks & 1) != 0;
            boolean didEarthBlock = (blocks & 2) != 0;
            boolean didFireBlock = (blocks & 4) != 0;
            boolean didWaterBlock = (blocks & 8) != 0;

            double pOutcome = pNow * choose(didAirBlock, airBlock)
                    * choose(didEarthBlock, earthBlock)
                    * choose(didFireBlock, fireBlock)
                    * choose(didWaterBlock, waterBlock);

            double damage = (didAirBlock ? 0 : airDmg)
                    + (didEarthBlock ? 0 : earthDmg)
                    + (didFireBlock ? 0 : fireDmg)
                    + (didWaterBlock ? 0 : waterDmg);

            i = addMyDamage(p, meHp, foeHp, round, pOutcome, meHpNow, foeHpNow, damage, roundNow + 1, i);
        }

        return i - 1;
    }


    private static final int MAX_STACK = 10000000;
    private static final ThreadLocal<double[]> P = ThreadLocal.withInitial(() -> new double[MAX_STACK]);
    private static final ThreadLocal<double[]> ME_HP = ThreadLocal.withInitial(() -> new double[MAX_STACK]);
    private static final ThreadLocal<double[]> FOE_HP = ThreadLocal.withInitial(() -> new double[MAX_STACK]);
    private static final ThreadLocal<int[]> ROUND = ThreadLocal.withInitial(() -> new int[MAX_STACK]);

    public record Results(double avgHealth, double winRate, double avgRounds, double totalP) {
    }

    // AI generated
    private static class Heap {

        private static void swap(double[] arr, int i, int j) {
            double temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }

        private static void swap(int[] arr, int i, int j) {
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }

        public static void heappush(int n, double[] p, double[] meHp, double[] foeHp, int[] round) {
            int child = n;
            while (child > 0) {
                int parent = (child - 1) / 2;
                if (p[child] > p[parent]) {
                    swap(p, parent, child);
                    swap(meHp, parent, child);
                    swap(foeHp, parent, child);
                    swap(round, parent, child);
                    child = parent;
                } else {
                    break;
                }
            }
        }

        public static void heappop(int n, double[] p, double[] meHp, double[] foeHp, int[] round) {
            if (n <= 0) {
                return; // Nothing to pop
            }
            // Swap root with last element
            swap(p, 0, n - 1);
            swap(meHp, 0, n - 1);
            swap(foeHp, 0, n - 1);
            swap(round, 0, n - 1);

            int parent = 0;
            int heapSize = n - 1;
            while (true) {
                int left = 2 * parent + 1;
                int right = 2 * parent + 2;
                int largest = parent;

                if (left < heapSize && p[left] > p[largest]) {
                    largest = left;
                }
                if (right < heapSize && p[right] > p[largest]) {
                    largest = right;
                }

                if (largest != parent) {
                    swap(p, parent, largest);
                    swap(meHp, parent, largest);
                    swap(foeHp, parent, largest);
                    swap(round, parent, largest);
                    parent = largest;
                } else {
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(new CombatSim(0.99, 100, 5, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0,
                60, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0).run());
        System.out.println(new CombatSim(0.99, 615, 5, 15, 61, 4, 0, 4, 0, 9, 0, 0, 0, 0, 0,
                650, 5, -20, 0, 0, 0, 30, 80, 0, 0, 0, 0, 0, 0, 0, 0).run());
    }
}
