public class Pair<Type> {
    public Type a;
    public Type b;

    public Pair(Type a, Type b) {
        // Pair should be unordered.
        if (a.hashCode() < b.hashCode()) {
            this.a = a;
            this.b = b;
        }
        else {
            this.a = b;
            this.b = a;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair) {
            Pair pair = (Pair) obj;
            return (this.a.equals(pair.a) && this.b.equals(pair.b)) || (this.a.equals(pair.b) && this.b.equals(pair.a));
        }
        return super.equals(obj);
    }

}
