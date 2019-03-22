package service;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import entities.Album;
import entities.Artist;
import entities.Song;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


public class FileService {
    private String path;
    private TreeMap<String, Artist> artistTreeMap;
    private ArrayList<File> files;

    private final String HEAD = "<head>\n" +
            "<meta charset=\"utf-8\">\n" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
            "<title>Catalog</title>\n" +
            "</head>\n";

    private final String HTML = "<html>\n";
    private final String HTML_END = "</html>\n";
    private final String BODY = "<body style=\"font: 12pt sans-serif\">\n";
    private final String BODY_END = "</body>\n";

    private final String UL = "<ul type=\"none\">\n";
    private final String UL_END = "</ul>\n";
    private final String LI = "<li>";
    private final String LI_END = "</li>\n";

    private final String B = "<b>";
    private final String B_END = "</b>\n";

    public FileService(String path) {
        this.path = path;
        this.artistTreeMap = new TreeMap<>();
        this.files = new ArrayList<>();
    }

    public void parseDirectories() throws InvalidDataException, IOException, UnsupportedTagException {
        long startTime = System.currentTimeMillis();
        File directory = new File(path);
        files = listFilesForFolder(directory);
        for (File file : files) {
            Mp3File mp3file = new Mp3File(file);
            if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                String artist = (id3v2Tag.getArtist() != null) ? (id3v2Tag.getArtist()) : ("Неизвестный исполнитель");
                String title = (id3v2Tag.getTitle() != null) ? (id3v2Tag.getTitle()) : ("Неизвестное название");
                String album = (id3v2Tag.getAlbum() != null) ? (id3v2Tag.getAlbum()) : ("Неизвестный альбом");
                Song song = new Song(title, file.getPath(), album, mp3file.getLengthInSeconds());
                if (artistTreeMap.get(artist) != null)
                    artistTreeMap.get(artist).addSong(song);
                else
                    artistTreeMap.put(artist, new Artist(artist, song));
            }
        }
        long finishTime = System.currentTimeMillis();
        System.out.println((finishTime - startTime) / 1000 + "sec - parseDirectories()");

    }

    //task 1
    public void genHTML(String path, String filename) throws FileNotFoundException, UnsupportedEncodingException {
        long startTime = System.currentTimeMillis();
        PrintWriter writer = new PrintWriter(path + "/" + filename + ".html", "UTF-8");
        String strHtml = HTML + HEAD + BODY;

        for (Artist Artist : artistTreeMap.values()) {
            strHtml += UL + LI + B + Artist.getName() + B_END + UL;
            for (Album album : Artist.getAlbums().values()) {
                strHtml += LI + B + album.getName() + B_END + UL;
                for (Song song : album.getSongList()) {
                    strHtml += LI + song.getName() + " " + Song.secondsToDuration(song.getDuration())
                            + "(<a href=\"file:///" + song.getPath() + "\">" + song.getPath() + "</a>)" + LI_END;
                }
                strHtml += UL_END + LI_END;
            }
            strHtml += UL_END + LI_END + UL_END;
        }

        strHtml += BODY_END + HTML_END;
        writer.println(strHtml);
        writer.close();
        long finishTime = System.currentTimeMillis();
        System.out.println((finishTime - startTime) / 1000 + "sec - genHTML()-task1");
    }

    //task 2
    public void genDuplHTMLList(String path, String filename) throws IOException {
        long startTime = System.currentTimeMillis();
        HashMap<File, String> fileStringHashMap = new HashMap<>();
        for (int i = 0; i < files.size() - 1; i++) {
            fileStringHashMap.put(files.get(i), DigestUtils.md5Hex(Files.newInputStream(Paths.get(files.get(i).getPath()))));
        }
        List<String> duplicates = fileStringHashMap.values().stream()
                .filter(i -> Collections.frequency(fileStringHashMap.values(), i) > 1)
                .distinct()
                .collect(Collectors.toList());

        PrintWriter writer = new PrintWriter(path + "/" + filename + ".html", "UTF-8");

        final String[] strHtml = {HTML + HEAD + BODY};
        int k = 1;

        for (String duplicate : duplicates) {
            strHtml[0] += UL + LI + B + "Дубликаты-" + (k++) + B_END + UL;
            fileStringHashMap.forEach((i, j) -> {
                if (j.equals(duplicate)) {
                    strHtml[0] += LI + i.getPath() + LI_END;

                }
            });
            strHtml[0] += UL_END + LI_END + UL_END;
        }
        strHtml[0] += BODY_END + HTML_END;
        writer.println(strHtml[0]);
        writer.close();
        long finishTime = System.currentTimeMillis();
        System.out.println((finishTime - startTime) / 1000 + "sec - genDuplHTMLList()-task2");
    }

    //task 3
    public void geneDupSongInflList(String path, String filename) throws FileNotFoundException, UnsupportedEncodingException {
        long startTime = System.currentTimeMillis();
        PrintWriter writer = new PrintWriter(path + "/" + filename + ".html", "UTF-8");
        String strHtml = HTML + HEAD + BODY + UL;

        for (Artist Artist : artistTreeMap.values()) {
            for (Album album : Artist.getAlbums().values()) {
                ArrayList<String> songsNames = new ArrayList<>();
                album.getSongList().forEach(e -> songsNames.add(e.getName()));
                List<Song> songs = album.getSongList().stream()
                        .filter(i -> Collections.frequency(songsNames, i.getName()) > 1)
                        .collect(Collectors.toList());
                if (!songs.isEmpty()) {
                    strHtml += LI + B + Artist.getName() + ", " + album.getName() + ", " + songs.get(0).getName() + B_END + UL;
                    for (Song song : songs) {
                        if (song.getName().equals(songs.get(0).getName()))
                            strHtml += LI + song.getPath() + LI_END;
                    }
                    songs.remove(0);
                    strHtml += UL_END + LI_END;
                }
            }
        }

        strHtml += UL_END + BODY_END + HTML_END;
        writer.println(strHtml);
        writer.close();
        long finishTime = System.currentTimeMillis();
        System.out.println((finishTime - startTime) / 1000 + "sec - geneDupSongInflList()-task3");
    }

    private ArrayList<File> listFilesForFolder(File folder) {
        ArrayList<File> files = new ArrayList<>();
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                files.addAll(listFilesForFolder(fileEntry));
            } else {
                if (fileEntry.getName().endsWith(".mp3"))
                    files.add(fileEntry);
            }
        }
        return files;
    }
}
