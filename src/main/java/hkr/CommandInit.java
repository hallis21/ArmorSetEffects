package hkr;

public class CommandInit {
    ArmorSetEffectsMain pl;
    public CommandInit(ArmorSetEffectsMain pl){
        this.pl = pl;
    }

	public void InitCommands() {
        reload();
	}
    
    private void reload() {
        pl.getCommand("armor-reload").setExecutor(new ReloadSets(pl));
        pl.getCommand("armor-list").setExecutor(new SetsCommand(pl));
    }
}