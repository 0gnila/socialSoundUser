package org.socialSound.socialSound;

import java.util.List;
public class Song 
{
    private String title;
    private String artist;
    private String googleDriveLink;
    private List<String> tags;
    
    public Song(String title, String artist, String googleDriveLink, List<String> tags) 
    {
        this.title = title;
        this.artist = artist;
        this.googleDriveLink = googleDriveLink;
        this.tags = tags;
    }
    public String getTitle() 
    {
        return title;
    }
    public String getArtist() 
    {
        return artist;
    }
    public String getGoogleDriveLink() 
    {
        return googleDriveLink;
    }
    public List<String> getTags() 
    {
        return tags;
    }
}