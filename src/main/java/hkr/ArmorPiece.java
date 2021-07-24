package hkr;

import org.bukkit.Material;

public class ArmorPiece {
    private Material item;
    private String[] metadata = new String[2];

    public ArmorPiece(){
    }
    public ArmorPiece(String item, String[] metadata) {
       this.item = Material.getMaterial(item, false);
       this.metadata = metadata;
    }
    public String toString(){
        return item.toString()+"\n    Name: "+ metadata[0]+"\n    Lore: "+metadata[1];
    }

    /**
     * @param item the item to set
     */
    public void setItem(String item) {
        this.item = Material.getMaterial(item);
    }
    /**
     * @param metadata the metadata to set
     */
    /**
     * @return the item
     */
    public Material getItem() {
        return item;
    }
    /**
     * @return the metadata
     */
    /**
     * @return the metadata
     */
    public String[] getMetadata() {
        return metadata;
    }
    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(String[] metadata) {
        this.metadata = metadata;
    }
}