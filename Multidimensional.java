import java.util.*;

public class Multidimensional {

    public static void main(String[] args) {
        // Example usage
        ArrayList<ArrayList<Integer>> D = new ArrayList<>();
        D.add(new ArrayList<>(Arrays.asList(1, 2, 6, 3)));
        D.add(new ArrayList<>(Arrays.asList(2, 6, 4, 5)));
        D.add(new ArrayList<>(Arrays.asList(4, 5)));

        // Utility function
        Set<Integer> F = new HashSet<>(Arrays.asList(1, 2, 4,3,5,  6));

        List<Set<Integer>> G = Arrays.asList(
                new HashSet<>(Arrays.asList(1, 2)),
                new HashSet<>(Arrays.asList(3, 4)),
                new HashSet<>(Arrays.asList(5, 6)),
                new HashSet<>(Arrays.asList(5))
        );

        int[] k = {1, 1, 1, 1};
        double alpha = 1.1;
        double xi = 0.01;

        Set<Integer> result = alpGreedy(D, F, G, k, alpha, xi);
        System.out.println("Result set is  S: " + result);
    }

    public static Set<Integer> alpGreedy(ArrayList<ArrayList<Integer>> D, Set<Integer> F, List<Set<Integer>> G, int[] k, double alpha, double xi) {
        double l = 0, h = 1;
        Set<Set<Integer>> S = new HashSet<>();

        while (h - l >= xi) {
            double epsilon = (l + h) / 2;
            List<Set<Integer>> setSystem = constructSetSystem(D, F, epsilon);
            Set<Set<Integer>> temp = solver(setSystem, F, G, k, alpha);
            if (temp.isEmpty()) {
                l = epsilon;
            } else {
                S = temp;
                h = epsilon;
            }
        }

        Set<Integer> s_points = new HashSet<>();
        for (Set<Integer> Pi : S) {
            s_points.addAll(Pi);
        }

        // Ensure fairness
        for (int i = 0; i < G.size(); i++) {
            while (s_points.stream().filter(G.get(i)::contains).count() < k[i]) {
                for (Integer pi : G.get(i)) {
                    if (!s_points.contains(pi)) {
                        s_points.add(pi);
                        break;
                    }
                }
            }
        }

        return s_points;
    }

    private static List<Set<Integer>> constructSetSystem(ArrayList<ArrayList<Integer>> D, Set<Integer> F, double epsilon) {
        List<Set<Integer>> setSystem = new ArrayList<>();
        for (ArrayList<Integer> pi : D) {
            Set<Integer> subset = new HashSet<>();
            for (Integer fj : F) {
                if (regRatio(pi, fj) <= epsilon) {
                    subset.add(fj);
                }
            }
            setSystem.add(subset);
        }
        return setSystem;
    }

    private static double regRatio(ArrayList<Integer> pi, Integer fj) {
        // Implement the regret ratio calculation based on your specific requirements
        // This is an example calculation
        int max = pi.stream().max(Integer::compareTo).orElse(Integer.MAX_VALUE);
        return (double) fj / max;
    }

    private static Set<Set<Integer>> solver(List<Set<Integer>> D, Set<Integer> F, List<Set<Integer>> G, int[] k, double alpha) {
        Set<Set<Integer>> S = new HashSet<>();
        Set<Integer> C = new HashSet<>();
        List<List<Set<Integer>>> levels = new ArrayList<>();

        int maxlevel = (int) Math.ceil(Math.log(D.size()) / Math.log(alpha)) + 1;
        for (int i = 0; i <= maxlevel; i++) {
            levels.add(new ArrayList<>());
        }

        for (Set<Integer> Pi : D) {
            int size = Pi.size();
            int level = (int) Math.floor(Math.log(size) / Math.log(alpha));
            if (level < 0) level = 0;  // Ensure level is non-negative
            if (level >= levels.size()) level = levels.size() - 1;  // Ensure level is within bounds
            levels.get(level).add(Pi);
        }

        for (int x = maxlevel; x >= 0; x--) {
            List<Set<Integer>> levelList = levels.get(x);
            for (Iterator<Set<Integer>> iterator = levelList.iterator(); iterator.hasNext(); ) {
                Set<Integer> Pi = iterator.next();
                if (anyGroupExceedsLimit(S, G, k) || Collections.disjoint(Pi, C)) {
                    iterator.remove();
                    continue;
                }

                if (Pi.stream().filter(e -> !C.contains(e)).count() >= Math.pow(alpha, x)) {
                    S.add(Pi);
                    C.addAll(Pi);
                    if (C.containsAll(F)) {
                        return S;
                    }
                } else {
                    int newLevel = x;
                    while (newLevel > 0 && Pi.stream().filter(e -> !C.contains(e)).count() < Math.pow(alpha, newLevel - 1)) {
                        newLevel--;
                    }
                    if (newLevel >= 0 && newLevel < levels.size()) {
                        levels.get(newLevel).add(Pi);
                    }
                }
            }
        }
        return C.containsAll(F) ? S : new HashSet<>();
    }

    private static boolean anyGroupExceedsLimit(Set<Set<Integer>> S, List<Set<Integer>> G, int[] k) {
        for (int i = 0; i < G.size(); i++) {
            long count = S.stream().flatMap(Set::stream).filter(G.get(i)::contains).count();
            if (count >= k[i]) {
                return true;
            }
        }
        return false;
    }
}
