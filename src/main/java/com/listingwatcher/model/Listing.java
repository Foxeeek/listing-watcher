package com.listingwatcher.model;

import java.time.OffsetDateTime;

public record Listing(long id,
                      String title,
                      String description,
                      String url,
                      OffsetDateTime createdAt,
                      OffsetDateTime refreshedAt,
                      Category category,
                      Location location,
                      Photos photos
) {
}
