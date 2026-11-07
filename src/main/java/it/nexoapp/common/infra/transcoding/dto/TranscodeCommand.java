package it.nexoapp.common.infra.transcoding.dto;

import it.nexoapp.common.domain.Date;
import it.nexoapp.common.infra.transcoding.TranscodingFacade;
import jakarta.validation.constraints.NotEmpty;

/**
 * Represents a command used for transcoding values from one domain/language
 * to another within a specified list category.
 *
 * @param list          The category or container where all translations are stored.
 * @param fromDomain    The originating domain of the value to be transcoded.
 * @param fromLanguage  The originating language of the value to be transcoded.
 * @param fromValue     The value to be transcoded.
 * @param toDomain      The target domain for the transcoded value.
 * @param toLanguage    The target language for the transcoded value.
 * @param date          The date associated with the transcoding command.
 */
public record TranscodeCommand(
        @NotEmpty
        String list,
        @NotEmpty
        String fromDomain,
        @NotEmpty
        String fromLanguage,
        @NotEmpty
        String fromValue,
        @NotEmpty
        String toDomain,
        @NotEmpty
        String toLanguage,
        Date date
){

    public static Builder builder() { return new Builder(); }

    /**
     * Builder class for constructing TranscodeCommand instances.
     */
    public static class Builder {
        private String list;
        private String fromDomain = TranscodingFacade.DEFAULT_DOMAIN;
        private String fromLanguage = TranscodingFacade.DEFAULT_LANGUAGE;
        private String fromValue;
        private String toDomain = TranscodingFacade.DEFAULT_DOMAIN;
        private String toLanguage = TranscodingFacade.DEFAULT_LANGUAGE;
        private Date date = Date.today();

        public Builder list(final String list) {
            this.list = list;
            return this;
        }

        public Builder fromDomain(final String fromDomain) {
            if(fromDomain != null) {
                this.fromDomain = fromDomain;
            }
            return this;
        }

        public Builder fromLanguage(final String fromLanguage) {
            if(fromLanguage != null) {
                this.fromLanguage = fromLanguage;
            }
            return this;
        }

        public Builder fromValue(final String fromValue) {
            this.fromValue = fromValue;
            return this;
        }

        public Builder toDomain(final String toDomain) {
            if(toDomain != null) {
                this.toDomain = toDomain;
            }
                return this;
        }

        public Builder toLanguage(final String toLanguage) {
            if(toLanguage != null) {
                this.toLanguage = toLanguage;
            }
            return this;
        }

        public Builder date(final Date date) {
            if(date != null) {
                this.date = date;
            }
            return this;
        }

        public TranscodeCommand build() {
            return new TranscodeCommand(
                    list,
                    fromDomain,
                    fromLanguage,
                    fromValue,
                    toDomain,
                    toLanguage,
                    date
            );
        }
    }

}