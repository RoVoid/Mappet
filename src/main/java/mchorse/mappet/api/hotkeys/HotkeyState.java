package mchorse.mappet.api.hotkeys;

public class HotkeyState {
    public String id;
    public boolean state;

    public static HotkeyState of(String id, boolean state) {
        HotkeyState hotkeyState = new HotkeyState();
        hotkeyState.id = id;
        hotkeyState.state = state;
        return hotkeyState;
    }
}
