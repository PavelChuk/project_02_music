package entities;

import java.util.TreeMap;

public class Artist {
    private String name;
    private TreeMap<String, Album> albums;

    public Artist(String name, Song song){
        this.name = name;
        this.albums = new TreeMap<>();
        addSong(song);
    }
    public String getName() {
        return name;
    }

    public TreeMap<String, Album> getAlbums() {
        return albums;
    }

    public void addSong(Song song){
        String nameAlbum = song.getNameAlbum();
        if(nameAlbum!=null&&albums.get(nameAlbum)!=null)
            albums.get(nameAlbum).addSong(song);
        else {
            albums.put(nameAlbum,new Album(song));
        }
    }
}
