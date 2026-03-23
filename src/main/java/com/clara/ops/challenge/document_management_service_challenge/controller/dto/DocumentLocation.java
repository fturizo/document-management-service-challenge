package com.clara.ops.challenge.document_management_service_challenge.controller.dto;

import java.util.concurrent.TimeUnit;

/**
 * Represents the location and expiration details of an uploaded document's temporary location.
 *
 * @param url The URL where the document can be downloaded.
 * @param expirationTime The duration after which the temporary location expires.
 * @param expirationUnit The unit of time for the expiration duration (e.g., seconds, minutes,
 *     hours).
 */
public record DocumentLocation(String url, int expirationTime, TimeUnit expirationUnit) {}
