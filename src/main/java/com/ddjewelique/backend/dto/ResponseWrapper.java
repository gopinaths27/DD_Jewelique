package com.ddjewelique.backend.dto;

public class ResponseWrapper<T> {
    private ResponseMessage<T> responseMessage;

    // Constructor
    public ResponseWrapper(String statusCode, String statsDescription, T message) {
        this.responseMessage = new ResponseMessage<>(statusCode, statsDescription, message);
    }

    // Getter & Setter
    public ResponseMessage<T> getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(ResponseMessage<T> responseMessage) {
        this.responseMessage = responseMessage;
    }

    // ✅ Inner class definition
    public static class ResponseMessage<T> {
        private String statusCode;
        private String statsDescription;
        private T message;

        public ResponseMessage(String statusCode, String statsDescription, T message) {
            this.statusCode = statusCode;
            this.statsDescription = statsDescription;
            this.message = message;
        }

        // Getters & Setters
        public String getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(String statusCode) {
            this.statusCode = statusCode;
        }

        public String getStatsDescription() {
            return statsDescription;
        }

        public void setStatsDescription(String statsDescription) {
            this.statsDescription = statsDescription;
        }

        public T getMessage() {
            return message;
        }

        public void setMessage(T message) {
            this.message = message;
        }
    }
}
