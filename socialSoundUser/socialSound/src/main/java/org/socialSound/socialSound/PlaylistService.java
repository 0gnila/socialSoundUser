
package org.socialSound.socialSound;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class PlaylistService {
    private MongoCollection<Document> songCollection;

    public PlaylistService(MongoCollection<Document> songCollection) {
        this.songCollection = songCollection;
    }

    // Fetch songs by tag
    public List<Song> getSongsByTag(String tag) {
        List<Song> songs = new ArrayList<>();
        try (MongoCursor<Document> cursor = songCollection.find(Filters.eq("tags", tag)).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String title = doc.getString("title");
                String artist = doc.getString("artist");
                String googleDriveLink = doc.getString("googleDriveLink");
                List<String> tags = (List<String>) doc.get("tags");
                songs.add(new Song(title, artist, googleDriveLink, tags));
            }
        }
        return songs;
    }

    // Fetch all available tags
    public List<String> getAllTags() {
        List<String> tags = new ArrayList<>();
        try (MongoCursor<Document> cursor = songCollection.aggregate(List.of(
                new Document("$unwind", "$tags"),
                new Document("$group", new Document("_id", "$tags"))
        )).iterator()) {
            while (cursor.hasNext()) {
                tags.add(cursor.next().getString("_id"));
            }
        }
        return tags;
    }
}

