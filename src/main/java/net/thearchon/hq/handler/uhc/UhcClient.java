package net.thearchon.hq.handler.uhc;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.GameClient;

public class UhcClient extends GameClient {

    private final String mode;
    
    public UhcClient(Archon archon, int id, String mode) {
        super(archon, ServerType.UHC, "uhc", id);

        this.mode = mode;
    }

    @Override
    public void setSlots(int slots) {
        if (getSlots() == 0) {
            super.setSlots(slots);
        }
    }
    
    public String getMode() {
        return mode;
    }
}
