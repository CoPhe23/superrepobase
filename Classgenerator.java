import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Classgenerator {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.print("Add meg a fájl nevét: ");
        String filename = input.nextLine().trim();

        System.out.print("Add meg a delimiter-t: ");
        String delimiter = input.nextLine();

        ArrayList<String> valtozoNevek = new ArrayList<>();
        ArrayList<String> tipusok = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(filename), "UTF-8")) {

            if (!scanner.hasNextLine()) {
                System.out.println("A fájl üres.");
                return;
            }

            String[] header = scanner.nextLine().split(delimiter, -1);

            for (String e : header) {
                valtozoNevek.add(atnevez(e));
                tipusok.add("unknown");
            }

            while (scanner.hasNextLine()) {
                String[] sor = scanner.nextLine().split(delimiter, -1);

                for (int i = 0; i < valtozoNevek.size(); i++) {
                    String ertek = i < sor.length ? sor[i] : "";
                    String ujTipus = detectType(ertek);
                    String regiTipus = tipusok.get(i);
                    tipusok.set(i, mergeType(regiTipus, ujTipus));
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("Nem található a fájl.");
            return;
        }

        String className = fajlnevbolOsztalynev(filename);

        try {
            generateClassFile(className, valtozoNevek, tipusok);
            generateMainFile(filename, delimiter, className, valtozoNevek, tipusok);
            System.out.println("Sikeres generálás:");
            System.out.println("- " + className + ".java");
            System.out.println("- Main.java");
        } catch (Exception e) {
            System.out.println("Hiba a fájlok generálása közben: " + e.getMessage());
        }
    }

    static void generateClassFile(String className, ArrayList<String> valtozoNevek, ArrayList<String> tipusok) throws Exception {
        try (PrintWriter writer = new PrintWriter(className + ".java")) {

            writer.println("public class " + className + " {");
            writer.println();

            for (int i = 0; i < valtozoNevek.size(); i++) {
                writer.println("    private " + javaType(tipusok.get(i)) + " " + valtozoNevek.get(i) + ";");
            }

            writer.println();

            writer.print("    public " + className + "(");
            for (int i = 0; i < valtozoNevek.size(); i++) {
                writer.print(javaType(tipusok.get(i)) + " " + valtozoNevek.get(i));
                if (i < valtozoNevek.size() - 1) {
                    writer.print(", ");
                }
            }
            writer.println(") {");

            for (String nev : valtozoNevek) {
                writer.println("        this." + nev + " = " + nev + ";");
            }

            writer.println("    }");
            writer.println();

            for (int i = 0; i < valtozoNevek.size(); i++) {
                String tipus = javaType(tipusok.get(i));
                String nev = valtozoNevek.get(i);

                writer.println("    public " + tipus + " get" + capitalize(nev) + "() {");
                writer.println("        return " + nev + ";");
                writer.println("    }");
                writer.println();
            }

            writer.println("    @Override");
            writer.println("    public String toString() {");
            writer.print("        return ");
            for (int i = 0; i < valtozoNevek.size(); i++) {
                if (i == 0) {
                    writer.print(valtozoNevek.get(i));
                } else {
                    writer.print(" + \";\" + " + valtozoNevek.get(i));
                }
            }
            writer.println(";");
            writer.println("    }");

            writer.println("}");
        }
    }

    static void generateMainFile(String filename, String delimiter, String className,
                                 ArrayList<String> valtozoNevek, ArrayList<String> tipusok) throws Exception {

        try (PrintWriter writer = new PrintWriter("Main.java")) {

            writer.println("import java.io.File;");
            writer.println("import java.io.FileNotFoundException;");
            writer.println("import java.io.IOException;");
            writer.println("import java.nio.file.Files;");
            writer.println("import java.nio.file.Path;");
            writer.println("import java.util.ArrayList;");
            writer.println("import java.util.List;");
            writer.println("import java.util.Random;");
            writer.println("import java.util.Scanner;");
            writer.println();
            writer.println("public class Main {");
            writer.println();
            writer.println("    public static void main(String[] args) {");
            writer.println("        ArrayList<" + className + "> lista = new ArrayList<>();");
            writer.println();
            writer.println("        try (Scanner scanner = new Scanner(new File(\"" + escapeJava(filename) + "\"), \"UTF-8\")) {");
            writer.println("            if (scanner.hasNextLine()) {");
            writer.println("                scanner.nextLine();");
            writer.println("            }");
            writer.println();
            writer.println("            while (scanner.hasNextLine()) {");
            writer.println("                String[] sor = scanner.nextLine().split(\"" + escapeJava(delimiter) + "\", -1);");
            writer.println();
            writer.println("                lista.add(new " + className + "(");

            for (int i = 0; i < valtozoNevek.size(); i++) {
                String expression = parseExpression(javaType(tipusok.get(i)), "sor[" + i + "]");
                if (i < valtozoNevek.size() - 1) {
                    writer.println("                        " + expression + ",");
                } else {
                    writer.println("                        " + expression);
                }
            }

            writer.println("                ));");
            writer.println("            }");
            writer.println();
            writer.println("        } catch (FileNotFoundException e) {");
            writer.println("            System.out.println(\"Nem található a fájl.\");");
            writer.println("            return;");
            writer.println("        }");
            writer.println();
            writer.println("        System.out.println(\"1) Beolvasott rekordok száma: \" + lista.size());");
            writer.println();
            writer.println("        //2es feladat");
            writer.println("    }");
            writer.println();

            writer.println("    static int parseIntSafe(String s) {");
            writer.println("        try {");
            writer.println("            if (s == null || s.isBlank()) return 0;");
            writer.println("            return Integer.parseInt(s.trim());");
            writer.println("        } catch (Exception e) {");
            writer.println("            return 0;");
            writer.println("        }");
            writer.println("    }");
            writer.println();

            writer.println("    static double parseDoubleSafe(String s) {");
            writer.println("        try {");
            writer.println("            if (s == null || s.isBlank()) return 0.0;");
            writer.println("            return Double.parseDouble(s.trim().replace(\",\", \".\"));");
            writer.println("        } catch (Exception e) {");
            writer.println("            return 0.0;");
            writer.println("        }");
            writer.println("    }");
            writer.println();

            writer.println("    static int countWords(String text) {");
            writer.println("        if (text == null || text.isBlank()) return 0;");
            writer.println("        return text.trim().split(\"\\\\s+\").length;");
            writer.println("    }");
            writer.println();

            writer.println("    static <T> T randomFromList(List<T> lista) {");
            writer.println("        if (lista == null || lista.isEmpty()) return null;");
            writer.println("        return lista.get(new Random().nextInt(lista.size()));");
            writer.println("    }");
            writer.println();

            writer.println("    static void writeLines(String fileName, List<String> sorok) {");
            writer.println("        try {");
            writer.println("            Files.write(Path.of(fileName), sorok);");
            writer.println("        } catch (IOException e) {");
            writer.println("            System.out.println(\"Hiba a fájlírás közben.\");");
            writer.println("        }");
            writer.println("    }");
            writer.println("}");
        }
    }

    static String parseExpression(String tipus, String source) {
        if (tipus.equals("int")) {
            return "parseIntSafe(" + source + ")";
        }
        if (tipus.equals("double")) {
            return "parseDoubleSafe(" + source + ")";
        }
        return source;
    }

    static String escapeJava(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    static String detectType(String e) {
        e = e.strip();

        if (e.isEmpty()) return "empty";

        try {
            Integer.parseInt(e);
            return "int";
        } catch (Exception ignored) {
        }

        try {
            Double.parseDouble(e.replace(",", "."));
            return "double";
        } catch (Exception ignored) {
        }

        return "String";
    }

    static String mergeType(String regi, String uj) {
        if (regi.equals("unknown") || regi.equals("empty")) return uj;
        if (uj.equals("empty")) return regi;
        if (regi.equals(uj)) return regi;

        if ((regi.equals("int") && uj.equals("double")) ||
                (regi.equals("double") && uj.equals("int"))) {
            return "double";
        }

        return "String";
    }

    static String javaType(String tipus) {
        if (tipus.equals("unknown") || tipus.equals("empty")) return "String";
        return tipus;
    }

    static String atnevez(String s) {
        s = s.toLowerCase().strip();

        s = s.replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ö", "o").replace("ő", "o")
                .replace("ú", "u").replace("ü", "u").replace("ű", "u");

        s = s.replaceAll("[()]", "");
        s = s.replaceAll("[^a-z0-9]", "_");
        s = s.replaceAll("_+", "_");
        s = s.replaceAll("^_|_$", "");

        if (s.isEmpty()) {
            return "mezo";
        }

        if (Character.isDigit(s.charAt(0))) {
            s = "f_" + s;
        }

        return s;
    }

    static String fajlnevbolOsztalynev(String filename) {
        String nev = filename;

        int slashIndex = Math.max(nev.lastIndexOf('/'), nev.lastIndexOf('\\'));
        if (slashIndex >= 0) {
            nev = nev.substring(slashIndex + 1);
        }

        if (nev.toLowerCase().endsWith(".csv")) {
            nev = nev.substring(0, nev.length() - 4);
        }

        nev = atnevez(nev);

        String[] darabok = nev.split("_");
        StringBuilder sb = new StringBuilder();

        for (String darab : darabok) {
            if (!darab.isBlank()) {
                sb.append(capitalize(darab));
            }
        }

        if (sb.length() == 0) {
            return "Adat";
        }

        return sb.toString();
    }

    static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}