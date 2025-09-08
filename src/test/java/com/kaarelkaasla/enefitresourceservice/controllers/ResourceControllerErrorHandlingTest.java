package com.kaarelkaasla.enefitresourceservice.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaarelkaasla.enefitresourceservice.dtos.*;
import com.kaarelkaasla.enefitresourceservice.entities.CharacteristicType;
import com.kaarelkaasla.enefitresourceservice.entities.ResourceType;
import com.kaarelkaasla.enefitresourceservice.exceptions.ResourceNotFoundException;
import com.kaarelkaasla.enefitresourceservice.services.ResourceService;
import com.kaarelkaasla.enefitresourceservice.services.TimeProvider;

@WebMvcTest(ResourceController.class)
class ResourceControllerErrorHandlingTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ResourceService resourceService;

  @MockitoBean private TimeProvider timeProvider;

  private ResourceRequest validCreateRequest;
  private ResourceRequest validUpdateRequest;
  private PatchResourceRequest validPatchRequest;

  @BeforeEach
  void setUp() {
    when(timeProvider.now()).thenReturn(OffsetDateTime.now());

    LocationRequest locationRequest =
        new LocationRequest("123 Main Street", "New York", "10001", "US");

    LocationRequest caLocationRequest =
        new LocationRequest("456 Queen Street", "Toronto", "12345", "CA");

    CharacteristicRequest characteristicRequest =
        new CharacteristicRequest("CT001", CharacteristicType.CONSUMPTION_TYPE, "Residential");

    validCreateRequest =
        new ResourceRequest(
            ResourceType.METERING_POINT, "US", locationRequest, Set.of(characteristicRequest));

    validUpdateRequest =
        new ResourceRequest(
            ResourceType.CONNECTION_POINT, "CA", caLocationRequest, Set.of(characteristicRequest));

    validPatchRequest =
        new PatchResourceRequest(
            Optional.of(ResourceType.METERING_POINT),
            Optional.of("US"),
            Optional.empty(),
            Optional.empty());
  }

  @Test
  void createResource_ServiceThrowsResourceNotFoundException_Returns404() throws Exception {
    when(resourceService.createResource(any(ResourceRequest.class)))
        .thenThrow(new ResourceNotFoundException("Resource not found"));

    mockMvc
        .perform(
            post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  void createResource_ServiceThrowsDataIntegrityViolationException_Returns409() throws Exception {
    when(resourceService.createResource(any(ResourceRequest.class)))
        .thenThrow(new DataIntegrityViolationException("Constraint violation"));

    mockMvc
        .perform(
            post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest)))
        .andExpect(status().isConflict());
  }

  @Test
  void createResource_ServiceThrowsGenericException_Returns500() throws Exception {
    when(resourceService.createResource(any(ResourceRequest.class)))
        .thenThrow(new RuntimeException("Internal server error"));

    mockMvc
        .perform(
            post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest)))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void getResourceById_ServiceThrowsResourceNotFoundException_Returns404() throws Exception {
    when(resourceService.getResourceById(999L))
        .thenThrow(new ResourceNotFoundException("Resource with ID 999 not found"));

    mockMvc
        .perform(get("/api/v1/resources/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Resource with ID 999 not found"));
  }

  @Test
  void updateResource_ServiceThrowsResourceNotFoundException_Returns404() throws Exception {
    when(resourceService.updateResource(eq(999L), any(ResourceRequest.class)))
        .thenThrow(new ResourceNotFoundException("Resource with ID 999 not found"));

    mockMvc
        .perform(
            put("/api/v1/resources/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  void updateResource_ServiceThrowsOptimisticLockingFailureException_Returns409() throws Exception {
    when(resourceService.updateResource(eq(1L), any(ResourceRequest.class)))
        .thenThrow(new OptimisticLockingFailureException("Version conflict"));

    mockMvc
        .perform(
            put("/api/v1/resources/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest)))
        .andExpect(status().isConflict());
  }

  @Test
  void patchResource_ServiceThrowsResourceNotFoundException_Returns404() throws Exception {
    when(resourceService.patchResource(eq(999L), any(PatchResourceRequest.class)))
        .thenThrow(new ResourceNotFoundException("Resource with ID 999 not found"));

    mockMvc
        .perform(
            patch("/api/v1/resources/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPatchRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  void patchResource_ServiceThrowsOptimisticLockingFailureException_Returns409() throws Exception {
    when(resourceService.patchResource(eq(1L), any(PatchResourceRequest.class)))
        .thenThrow(new OptimisticLockingFailureException("Version conflict"));

    mockMvc
        .perform(
            patch("/api/v1/resources/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPatchRequest)))
        .andExpect(status().isConflict());
  }

  @Test
  void deleteResource_ServiceThrowsResourceNotFoundException_Returns404() throws Exception {
    doThrow(new ResourceNotFoundException("Resource with ID 999 not found"))
        .when(resourceService)
        .deleteResource(999L);

    mockMvc.perform(delete("/api/v1/resources/999")).andExpect(status().isNotFound());
  }

  @Test
  void deleteResource_ServiceThrowsOptimisticLockingFailureException_Returns409() throws Exception {
    doThrow(new OptimisticLockingFailureException("Version conflict"))
        .when(resourceService)
        .deleteResource(1L);

    mockMvc.perform(delete("/api/v1/resources/1")).andExpect(status().isConflict());
  }

  @Test
  void sendAllResources_ServiceThrowsException_Returns500() throws Exception {
    when(resourceService.notifyAllResources())
        .thenThrow(new RuntimeException("Kafka service unavailable"));

    mockMvc.perform(post("/api/v1/resources/send-all")).andExpect(status().isInternalServerError());
  }

  @Test
  void createResource_InvalidContentType_Returns415() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_XML)
                .content("<xml>invalid</xml>"))
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  void createResource_MalformedJson_Returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"invalid\": \"json\""))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createResource_MissingRequiredFields_Returns400() throws Exception {
    ResourceRequest invalidRequest = new ResourceRequest(null, null, null, null);

    mockMvc
        .perform(
            post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors").isArray());
  }

  @Test
  void createResource_InvalidCountryCode_Returns400() throws Exception {
    ResourceRequest invalidRequest =
        new ResourceRequest(
            ResourceType.METERING_POINT,
            "INVALID",
            new LocationRequest("123 Main St", "City", "12345", "US"),
            Set.of());

    mockMvc
        .perform(
            post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createResource_InvalidPostalCode_Returns400() throws Exception {
    LocationRequest invalidLocation = new LocationRequest("123 Main St", "City", "ABC123", "US");

    ResourceRequest invalidRequest =
        new ResourceRequest(ResourceType.METERING_POINT, "US", invalidLocation, Set.of());

    mockMvc
        .perform(
            post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createResource_CharacteristicCodeTooLong_Returns400() throws Exception {
    CharacteristicRequest invalidCharacteristic =
        new CharacteristicRequest("TOOLONG", CharacteristicType.CONSUMPTION_TYPE, "Value");

    ResourceRequest invalidRequest =
        new ResourceRequest(
            ResourceType.METERING_POINT,
            "US",
            new LocationRequest("123 Main St", "City", "12345", "US"),
            Set.of(invalidCharacteristic));

    mockMvc
        .perform(
            post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors").isArray());
  }

  @Test
  void updateResource_MissingRequestBody_Returns400() throws Exception {
    mockMvc
        .perform(put("/api/v1/resources/1").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void patchResource_MissingRequestBody_Returns400() throws Exception {
    mockMvc
        .perform(patch("/api/v1/resources/1").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void patchResource_InvalidEnumValue_Returns400() throws Exception {
    String invalidPatchJson =
        """
        {
          "type": "METERING_POINT2"
        }
        """;

    mockMvc
        .perform(
            patch("/api/v1/resources/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPatchJson))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Invalid enum value in request body"));
  }

  @Test
  void createResource_InvalidEnumValue_Returns400() throws Exception {
    String invalidCreateJson =
        """
        {
          "type": "INVALID_TYPE",
          "countryCode": "US",
          "location": {
            "streetAddress": "123 Main St",
            "city": "City",
            "postalCode": "12345",
            "countryCode": "US"
          },
          "characteristics": []
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidCreateJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Invalid enum value in request body"));
  }

  @Test
  void getAllEndpoints_InvalidHttpMethod_Returns405() throws Exception {
    mockMvc.perform(post("/api/v1/resources/1")).andExpect(status().isMethodNotAllowed());
    mockMvc.perform(put("/api/v1/resources")).andExpect(status().isMethodNotAllowed());
    mockMvc.perform(delete("/api/v1/resources")).andExpect(status().isMethodNotAllowed());
  }
}
