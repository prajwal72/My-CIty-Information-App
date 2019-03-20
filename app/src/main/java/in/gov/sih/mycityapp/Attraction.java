package in.gov.sih.mycityapp;

public class Attraction {

    String name, description, imageURL;
    float rating, numberOfReviews;

    public Attraction(String name, String description, String imageURL, float rating, float numberOfReviews) {
        this.name = name;
        this.description = description;
        this.imageURL = imageURL;
        this.rating = rating;
        this.numberOfReviews = numberOfReviews;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public float getNumberOfReviews() {
        return numberOfReviews;
    }

    public void setNumberOfReviews(float numberOfReviews) {
        this.numberOfReviews = numberOfReviews;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
