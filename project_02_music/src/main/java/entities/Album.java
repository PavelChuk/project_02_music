package entities;

import java.util.ArrayList;

public class Album {
    private String name;
    private ArrayList<Song> songList;

    public Album(Song song){
        this.name = song.getNameAlbum();
        this.songList = new ArrayList<Song>();
        songList.add(song);
    }
    public String getName() {
        return name;
    }

    public ArrayList<Song> getSongList() {
        return songList;
    }

    public void addSong(Song song){
        songList.add(song);
    }

    public String describe(){
        String string = "\t Album: "+this.name+"\n";
        for(Song song:songList)
            string+=song.describe();
        return string;
    }
}
