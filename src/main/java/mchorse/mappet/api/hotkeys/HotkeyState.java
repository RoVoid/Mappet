package mchorse.mappet.api.hotkeys;

public class HotkeyState {
    public String name;
    public boolean state;

    public static HotkeyState of(String name, boolean state) {
        HotkeyState hotkeyState = new HotkeyState();
        hotkeyState.name = name;
        hotkeyState.state = state;
        return hotkeyState;
    }
}
