package com.kaarelkaasla.enefitresourceservice.api;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.kaarelkaasla.enefitresourceservice.dtos.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * API contract for managing resources used by the controller layer.
 * Annotated for OpenAPI/Swagger to document requests and responses.
 */
@Tag(
    name = "Resource Management",
    description = "APIs for managing metering points and connection points")
public interface ResourceApi {

  @Operation(
      summary = "Create a new resource",
      description =
          "Creates a new metering point or connection point with location and characteristics",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Resource creation request",
              required = true,
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = ResourceRequest.class),
                      examples = {
                        @ExampleObject(
                            name = "Metering Point Request",
                            summary = "Create a metering point in the US",
                            value =
                                """
                          {
                            "type": "METERING_POINT",
                            "countryCode": "US",
                            "location": {
                              "streetAddress": "123 Main Street",
                              "city": "New York",
                              "postalCode": "10001",
                              "countryCode": "US"
                            },
                            "characteristics": [
                              {
                                "code": "CT001",
                                "type": "CONSUMPTION_TYPE",
                                "value": "Residential"
                              }
                            ]
                          }
                          """),
                        @ExampleObject(
                            name = "Connection Point Request",
                            summary = "Create a connection point in Canada",
                            value =
                                """
                          {
                            "type": "CONNECTION_POINT",
                            "countryCode": "CA",
                            "location": {
                              "streetAddress": "456 Oak Avenue",
                              "city": "Toronto",
                              "postalCode": "12345",
                              "countryCode": "CA"
                            },
                            "characteristics": [
                              {
                                "code": "CP001",
                                "type": "CHARGING_POINT",
                                "value": "Fast Charging"
                              }
                            ]
                          }
                          """)
                      })))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Resource created successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ResourceResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Created Resource",
                            summary = "Successfully created metering point",
                            value =
                                """
                                            {
                                              "id": 1,
                                              "type": "METERING_POINT",
                                              "countryCode": "US",
                                              "version": 1,
                                              "createdAt": "2024-01-15T10:30:00Z",
                                              "updatedAt": "2024-01-15T10:30:00Z",
                                              "location": {
                                                "streetAddress": "123 Main Street",
                                                "city": "New York",
                                                "postalCode": "10001",
                                                "countryCode": "US"
                                              },
                                              "characteristics": [
                                                {
                                                  "code": "CT001",
                                                  "type": "CONSUMPTION_TYPE",
                                                  "value": "Residential"
                                                }
                                              ]
                                            }
                                            """))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data - validation errors",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                      @ExampleObject(
                          name = "Validation Error",
                          summary = "Invalid country code validation failure",
                          value =
                              """
                                                    {
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Validation failed",
                                                      "path": "/api/v1/resources",
                                                      "timestamp": "2024-01-15T10:30:00",
                                                      "fieldErrors": [
                                                        {
                                                          "field": "countryCode",
                                                          "rejectedValue": "XX",
                                                          "message": "Invalid country code 'XX'. Must be a valid ISO 3166-1 alpha-2 code"
                                                        }
                                                      ]
                                                    }
                                                    """),
                      @ExampleObject(
                          name = "Missing Required Field",
                          summary = "Required field missing validation failure",
                          value =
                              """
                                                    {
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Validation failed",
                                                      "path": "/api/v1/resources",
                                                      "timestamp": "2024-01-15T10:30:00",
                                                      "fieldErrors": [
                                                        {
                                                          "field": "location.postalCode",
                                                          "rejectedValue": null,
                                                          "message": "Postal code is required"
                                                        }
                                                      ]
                                                    }
                                                    """)
                    })),
        @ApiResponse(
            responseCode = "409",
            description = "Constraint violation - resource already exists or data conflict",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Duplicate Resource",
                            summary = "Resource with same characteristics already exists",
                            value =
                                """
                                            {
                                              "status": 409,
                                              "error": "Conflict",
                                              "message": "Resource with similar characteristics already exists",
                                              "path": "/api/v1/resources",
                                              "timestamp": "2024-01-15T10:30:00",
                                              "fieldErrors": []
                                            }
                                            """))),
        @ApiResponse(
            responseCode = "415",
            description = "Unsupported media type - incorrect Content-Type header",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Wrong Content Type",
                            summary = "Request sent with unsupported media type",
                            value =
                                """
                                            {
                                              "status": 415,
                                              "error": "Unsupported Media Type",
                                              "message": "Content type 'text/plain' not supported. Expected 'application/json'",
                                              "path": "/api/v1/resources",
                                              "timestamp": "2024-01-15T10:30:00",
                                              "fieldErrors": []
                                            }
                                            """)))
      })
  ResponseEntity<ResourceResponse> createResource(@Valid @RequestBody ResourceRequest request);

  @Operation(
      summary = "Get all resources",
      description = "Retrieves all resources with their location and characteristics")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resources retrieved successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ResourceResponse.class, type = "array"),
                    examples =
                        @ExampleObject(
                            name = "Resource List",
                            summary = "List of all resources",
                            value =
                                """
                                            [
                                              {
                                                "id": 1,
                                                "type": "METERING_POINT",
                                                "countryCode": "US",
                                                "version": 1,
                                                "createdAt": "2024-01-15T10:30:00Z",
                                                "updatedAt": "2024-01-15T10:30:00Z",
                                                "location": {
                                                  "streetAddress": "123 Main Street",
                                                  "city": "New York",
                                                  "postalCode": "10001",
                                                  "countryCode": "US"
                                                },
                                                "characteristics": [
                                                  {
                                                    "code": "CT001",
                                                    "type": "CONSUMPTION_TYPE",
                                                    "value": "Residential"
                                                  }
                                                ]
                                              },
                                              {
                                                "id": 2,
                                                "type": "CONNECTION_POINT",
                                                "countryCode": "CA",
                                                "version": 1,
                                                "createdAt": "2024-01-15T11:00:00Z",
                                                "updatedAt": "2024-01-15T11:00:00Z",
                                                "location": {
                                                  "streetAddress": "456 Oak Avenue",
                                                  "city": "Toronto",
                                                  "postalCode": "12345",
                                                  "countryCode": "CA"
                                                },
                                                "characteristics": [
                                                  {
                                                    "code": "CP001",
                                                    "type": "CHARGING_POINT",
                                                    "value": "Fast Charging"
                                                  }
                                                ]
                                              }
                                            ]
                                            """)))
      })
  ResponseEntity<List<ResourceResponse>> getAllResources();

  @Operation(
      summary = "Get resource by ID",
      description = "Retrieves a specific resource by its ID with location and characteristics")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resource retrieved successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ResourceResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Found Resource",
                            summary = "Successfully retrieved resource by ID",
                            value =
                                """
                                            {
                                              "id": 1,
                                              "type": "METERING_POINT",
                                              "countryCode": "US",
                                              "version": 1,
                                              "createdAt": "2024-01-15T10:30:00Z",
                                              "updatedAt": "2024-01-15T10:30:00Z",
                                              "location": {
                                                "streetAddress": "123 Main Street",
                                                "city": "New York",
                                                "postalCode": "10001",
                                                "countryCode": "US"
                                              },
                                              "characteristics": [
                                                {
                                                  "code": "CT001",
                                                  "type": "CONSUMPTION_TYPE",
                                                  "value": "Residential"
                                                }
                                              ]
                                            }
                                            """))),
        @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Resource Not Found",
                            summary = "No resource exists with the given ID",
                            value =
                                """
                                            {
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Resource with ID 999 not found",
                                              "path": "/api/v1/resources/999",
                                              "timestamp": "2024-01-15T10:30:00",
                                              "fieldErrors": []
                                            }
                                            """)))
      })
  ResponseEntity<ResourceResponse> getResourceById(
      @Parameter(description = "Resource ID", required = true, example = "1") @PathVariable
          Long id);

  @Operation(
      summary = "Update resource (full update)",
      description =
          "Updates all fields of a resource. Uses optimistic locking for concurrency control.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Resource update request with all fields",
              required = true,
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = ResourceRequest.class),
                      examples = {
                        @ExampleObject(
                            name = "Update to Connection Point",
                            summary = "Update resource type and location to Canada",
                            value =
                                """
                          {
                            "type": "CONNECTION_POINT",
                            "countryCode": "CA",
                            "location": {
                              "streetAddress": "456 Oak Avenue",
                              "city": "Toronto",
                              "postalCode": "12345",
                              "countryCode": "CA"
                            },
                            "characteristics": [
                              {
                                "code": "CP002",
                                "type": "CHARGING_POINT",
                                "value": "Fast Charging"
                              }
                            ]
                          }
                          """),
                        @ExampleObject(
                            name = "Update to Metering Point",
                            summary = "Update resource to US metering point",
                            value =
                                """
                          {
                            "type": "METERING_POINT",
                            "countryCode": "US",
                            "location": {
                              "streetAddress": "789 Pine Street",
                              "city": "San Francisco",
                              "postalCode": "94102",
                              "countryCode": "US"
                            },
                            "characteristics": [
                              {
                                "code": "MT001",
                                "type": "METER_TYPE",
                                "value": "Smart Meter"
                              }
                            ]
                          }
                          """)
                      })))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resource updated successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ResourceResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Updated Resource",
                            summary = "Successfully updated resource with new version",
                            value =
                                """
                                            {
                                              "id": 1,
                                              "type": "CONNECTION_POINT",
                                              "countryCode": "CA",
                                              "version": 2,
                                              "createdAt": "2024-01-15T10:30:00Z",
                                              "updatedAt": "2024-01-15T12:15:00Z",
                                              "location": {
                                                "streetAddress": "456 Oak Avenue",
                                                "city": "Toronto",
                                                "postalCode": "12345",
                                                "countryCode": "CA"
                                              },
                                              "characteristics": [
                                                {
                                                  "code": "CP002",
                                                  "type": "CHARGING_POINT",
                                                  "value": "Fast Charging"
                                                }
                                              ]
                                            }
                                            """))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Invalid Postal Code",
                            summary = "Postal code format validation failure",
                            value =
                                """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Validation failed",
                                              "path": "/api/v1/resources/1",
                                              "timestamp": "2024-01-15T10:30:00",
                                              "fieldErrors": [
                                                {
                                                  "field": "location.postalCode",
                                                  "rejectedValue": "ABC123",
                                                  "message": "Postal code must be exactly 5 digits"
                                                }
                                              ]
                                            }
                                            """))),
        @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Resource Not Found",
                            summary = "No resource exists with the given ID for update",
                            value =
                                """
                                            {
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Resource with ID 999 not found",
                                              "path": "/api/v1/resources/999",
                                              "timestamp": "2024-01-15T10:30:00",
                                              "fieldErrors": []
                                            }
                                            """))),
        @ApiResponse(
            responseCode = "409",
            description = "Optimistic locking conflict - resource was modified by another process",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Version Conflict",
                            summary = "Resource was modified by another user/process",
                            value =
                                """
                                            {
                                              "status": 409,
                                              "error": "Conflict",
                                              "message": "Resource has been modified by another process. Current version is 3, but you are trying to update version 2",
                                              "path": "/api/v1/resources/1",
                                              "timestamp": "2024-01-15T10:30:00",
                                              "fieldErrors": []
                                            }
                                            """))),
        @ApiResponse(
            responseCode = "415",
            description = "Unsupported media type",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Wrong Content Type",
                            summary = "Request sent with unsupported media type",
                            value =
                                """
                                            {
                                              "status": 415,
                                              "error": "Unsupported Media Type",
                                              "message": "Content type 'text/plain' not supported. Expected 'application/json'",
                                              "path": "/api/v1/resources/1",
                                              "timestamp": "2024-01-15T10:30:00",
                                              "fieldErrors": []
                                            }
                                            """)))
      })
  ResponseEntity<ResourceResponse> updateResource(
      @Parameter(description = "Resource ID", required = true, example = "1") @PathVariable Long id,
      @Valid @RequestBody ResourceRequest request);

  @Operation(
      summary = "Patch resource (partial update)",
      description =
          "Partially updates a resource with only the provided fields. Uses optimistic locking for"
              + " concurrency control.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Resource patch request with optional fields",
              required = true,
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = PatchResourceRequest.class),
                      examples = {
                        @ExampleObject(
                            name = "Update Location Only",
                            summary = "Change only the location to Vancouver, CA",
                            value =
                                """
                          {
                            "location": {
                              "streetAddress": "789 Pine Street",
                              "city": "Vancouver",
                              "postalCode": "67890",
                              "countryCode": "CA"
                            },
                            "countryCode": "CA"
                          }
                          """),
                        @ExampleObject(
                            name = "Update Characteristics Only",
                            summary = "Change only the characteristics",
                            value =
                                """
                          {
                            "characteristics": [
                              {
                                "code": "CPS03",
                                "type": "CONNECTION_POINT_STATUS",
                                "value": "Active"
                              }
                            ]
                          }
                          """),
                        @ExampleObject(
                            name = "Update Type and Country",
                            summary = "Change resource type and country with matching location",
                            value =
                                """
                          {
                            "type": "METERING_POINT",
                            "countryCode": "US",
                            "location": {
                              "streetAddress": "123 Oak Street",
                              "city": "Boston",
                              "postalCode": "02101",
                              "countryCode": "US"
                            }
                          }
                          """)
                      })))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resource patched successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ResourceResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Patched Resource",
                            summary = "Successfully updated only specified fields",
                            value =
                                """
                                            {
                                              "id": 1,
                                              "type": "METERING_POINT",
                                              "countryCode": "CA",
                                              "version": 2,
                                              "createdAt": "2024-01-15T10:30:00Z",
                                              "updatedAt": "2024-01-15T12:45:00Z",
                                              "location": {
                                                "streetAddress": "789 Pine Street",
                                                "city": "Vancouver",
                                                "postalCode": "67890",
                                                "countryCode": "CA"
                                              },
                                              "characteristics": [
                                                {
                                                  "code": "CPS03",
                                                  "type": "CONNECTION_POINT_STATUS",
                                                  "value": "Active"
                                                }
                                              ]
                                            }
                                            """))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Invalid Characteristic Code",
                            summary = "Characteristic code too long",
                            value =
                                """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Validation failed",
                                              "path": "/api/v1/resources/1",
                                              "timestamp": "2024-01-15T10:30:00",
                                              "fieldErrors": [
                                                {
                                                  "field": "characteristics[0].code",
                                                  "rejectedValue": "TOOLONG",
                                                  "message": "Invalid characteristic code 'TOOLONG'. Must be maximum 5 characters, but was 7"
                                                }
                                              ]
                                            }
                                            """))),
        @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Resource Not Found",
                            summary = "No resource exists with the given ID for patch",
                            value =
                                """
                                            {
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Resource with ID 999 not found",
                                              "path": "/api/v1/resources/999",
                                              "timestamp": "2024-01-15T10:30:00",
                                              "fieldErrors": []
                                            }
                                            """))),
        @ApiResponse(
            responseCode = "409",
            description = "Optimistic locking conflict",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Version Conflict",
                            summary = "Resource was modified during patch operation",
                            value =
                                """
                                            {
                                              "status": 409,
                                              "error": "Conflict",
                                              "message": "Resource has been modified by another process. Please refresh and try again",
                                              "path": "/api/v1/resources/1",
                                              "timestamp": "2024-01-15T10:30:00",
                                              "fieldErrors": []
                                            }
                                            """))),
        @ApiResponse(
            responseCode = "415",
            description = "Unsupported media type",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Wrong Content Type",
                            summary = "Request sent with unsupported media type",
                            value =
                                """
                                            {
                                              "status": 415,
                                              "error": "Unsupported Media Type",
                                              "message": "Content type 'application/xml' not supported. Expected 'application/json'",
                                              "path": "/api/v1/resources/1",
                                              "timestamp": "2024-01-15T10:30:00",
                                              "fieldErrors": []
                                            }
                                            """)))
      })
  ResponseEntity<ResourceResponse> patchResource(
      @Parameter(description = "Resource ID", required = true, example = "1") @PathVariable Long id,
      @Valid @RequestBody PatchResourceRequest request);

  @Operation(
      summary = "Delete resource",
      description =
          "Deletes a resource by its ID. Uses optimistic locking for concurrency control.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "Resource deleted successfully - no content returned"),
        @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Resource Not Found",
                            summary = "No resource exists with the given ID for deletion",
                            value =
                                """
                                            {
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Resource with ID 999 not found",
                                              "path": "/api/v1/resources/999",
                                              "timestamp": "2024-01-15T10:30:00",
                                              "fieldErrors": []
                                            }
                                            """))),
        @ApiResponse(
            responseCode = "409",
            description = "Optimistic locking conflict or resource in use",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                      @ExampleObject(
                          name = "Version Conflict",
                          summary = "Resource was modified during deletion",
                          value =
                              """
                                                    {
                                                      "status": 409,
                                                      "error": "Conflict",
                                                      "message": "Resource has been modified by another process and cannot be deleted",
                                                      "path": "/api/v1/resources/1",
                                                      "timestamp": "2024-01-15T10:30:00",
                                                      "fieldErrors": []
                                                    }
                                                    """),
                      @ExampleObject(
                          name = "Resource In Use",
                          summary = "Resource cannot be deleted due to dependencies",
                          value =
                              """
                                                    {
                                                      "status": 409,
                                                      "error": "Conflict",
                                                      "message": "Resource cannot be deleted because it is referenced by other entities",
                                                      "path": "/api/v1/resources/1",
                                                      "timestamp": "2024-01-15T10:30:00",
                                                      "fieldErrors": []
                                                    }
                                                    """)
                    }))
      })
  ResponseEntity<Void> deleteResource(
      @Parameter(description = "Resource ID", required = true, example = "1") @PathVariable
          Long id);

  @Operation(
      summary = "Send all resources for batch notification",
      description = "Publishes all resources to the Kafka topic for batch notification")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Batch notification completed successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = BatchNotificationResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Batch Success",
                            summary = "All resources successfully published to Kafka",
                            value =
                                """
                                            {
                                              "operationId": "550e8400-e29b-41d4-a716-446655440000",
                                              "resourceCount": 250,
                                              "status": "COMPLETED",
                                              "processedAt": "2024-01-15T14:30:00Z",
                                              "operation": "BATCH_NOTIFICATION"
                                            }
                                            """))),
        @ApiResponse(
            responseCode = "500",
            description = "Service unavailable - Kafka or internal system error",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                      @ExampleObject(
                          name = "Kafka Unavailable",
                          summary = "Kafka service is down or unreachable",
                          value =
                              """
                                                    {
                                                      "status": 500,
                                                      "error": "Internal Server Error",
                                                      "message": "Kafka service is temporarily unavailable. Please try again later",
                                                      "path": "/api/v1/resources/send-all",
                                                      "timestamp": "2024-01-15T10:30:00",
                                                      "fieldErrors": []
                                                    }
                                                    """),
                      @ExampleObject(
                          name = "Batch Processing Failed",
                          summary = "Error occurred during batch processing",
                          value =
                              """
                                                    {
                                                      "status": 500,
                                                      "error": "Internal Server Error",
                                                      "message": "Failed to process batch notification: Database connection timeout",
                                                      "path": "/api/v1/resources/send-all",
                                                      "timestamp": "2024-01-15T10:30:00",
                                                      "fieldErrors": []
                                                    }
                                                    """)
                    }))
      })
  ResponseEntity<BatchNotificationResponse> sendAllResources();
}
