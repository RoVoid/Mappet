package mchorse.mappet.addon;

public interface IMappetAddon {
    String id();

    String name();

    String author();

    String description();

    String version();

    String url();

    default String str() {
        return name() + " (" + id() + ":" + version() + " by " + author() + ")";
    }
}
