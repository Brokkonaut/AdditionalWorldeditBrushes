package de.iani.additionalWorldeditBrushes;

public class ArgsParser {
    private String[] args;

    private int current;

    public ArgsParser(String[] args) {
        this.args = args;
        this.current = -1;
    }

    public ArgsParser(String[] args, int skipParts) {
        this.args = args;
        this.current = -1 + skipParts;
    }

    public boolean hasNext() {
        return current < args.length - 1;
    }

    public int remaining() {
        return Math.max(args.length - 1 - current, 0);
    }

    public String getAll(String def) {
        ++current;
        if (args.length <= current) {
            return def;
        }
        StringBuilder sb = new StringBuilder();
        while (args.length > current) {
            sb.append(args[current]);
            sb.append(' ');
            ++current;
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public String seeNext(String def) {
        if (args.length <= current + 1) {
            return def;
        }
        return args[current + 1];
    }

    public String getNext(String def) {
        ++current;
        if (args.length <= current) {
            return def;
        }
        return args[current];
    }

    public int getNext(int def) {
        String next = getNext(null);
        if (next == null) {
            return def;
        }
        try {
            return Integer.parseInt(next);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public double getNext(double def) {
        String next = getNext(null);
        if (next == null) {
            return def;
        }
        try {
            return Double.parseDouble(next);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public Boolean getNext(boolean ignored) {
        String next = getNext(null);
        if (next == null) {
            return null;
        }
        if (next.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        } else if (next.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        return null;
    }
}
