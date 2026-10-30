package com.cti.provedor.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/** O banco guarda UTC; a API devolve Instant (ISO-8601 com "Z"). */
public final class Datas {

    private Datas() {}

    public static LocalDateTime agoraUtc() { return LocalDateTime.now(ZoneOffset.UTC); }

    public static Instant paraInstant(LocalDateTime v) { return v == null ? null : v.toInstant(ZoneOffset.UTC); }
}
