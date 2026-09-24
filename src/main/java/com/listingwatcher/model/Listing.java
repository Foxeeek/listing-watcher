package com.listingwatcher.model;

import com.listingwatcher.provider.Category;
import com.listingwatcher.provider.Location;
import com.listingwatcher.provider.Photo;

import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;

public record Listing(int id,
                      String title,
                      String description,
                      String url,
                      Category category,
                      Location location,
                      OffsetDateTime created_time,
                      OffsetDateTime last_refresh_time,
                      List<Photo> photos) {
}


