#!/bin/bash


set -e

echo "🚀 Kafka Failure Testing Script"
echo "==============================="

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

wait_for_service() {
    local service_url=$1
    local service_name=$2
    log_info "Waiting for $service_name to be ready..."
    
    for i in {1..30}; do
        if curl -s "$service_url" > /dev/null 2>&1; then
            log_info "$service_name is ready!"
            return 0
        fi
        sleep 2
    done
    
    log_error "$service_name is not ready after 60 seconds"
    return 1
}

create_test_resource() {
    log_info "Creating test resource..."
    curl -s -X POST http://localhost:18080/api/v1/resources \
        -H "Content-Type: application/json" \
        -d '{
            "type": "METERING_POINT",
            "countryCode": "US",
            "location": {
                "streetAddress": "123 Test Street",
                "city": "Test City", 
                "postalCode": "12345",
                "countryCode": "US"
            },
            "characteristics": []
        }' | jq -r '.id // empty'
}

send_batch_notification() {
    log_info "Sending batch notification..."
    local response=$(curl -s -X POST http://localhost:18080/api/v1/resources/send-all)
    echo "$response" | jq .
}

case "${1:-help}" in
    "scenario1")
        log_info "🧪 Testing Scenario 1: Kafka Down During Event Publishing"
        echo
        
        log_info "Step 1: Starting all services..."
        docker-compose up -d
        
        wait_for_service "http://localhost:18080/actuator/health" "Application"
        wait_for_service "http://localhost:18081" "Kafka UI"
        
        log_info "Step 2: Creating test data..."
        test_resource_id=$(create_test_resource)
        if [ -n "$test_resource_id" ]; then
            log_info "Created resource with ID: $test_resource_id"
        fi
        
        log_info "Step 3: Stopping Kafka container..."
        docker stop enefitresourceservice-kafka
        
        log_info "Step 4: Attempting batch notification (should return success immediately)..."
        send_batch_notification
        
        log_warn "Check application logs for Kafka connection errors:"
        echo "docker logs enefitresourceservice-app --tail=20"
        echo
        log_warn "To complete this test:"
        echo "1. Watch logs: docker logs -f enefitresourceservice-app"
        echo "2. Restart Kafka: docker start enefitresourceservice-kafka"
        echo "3. Send another batch: curl -X POST http://localhost:18080/api/v1/resources/send-all"
        ;;
        
    "scenario2") 
        log_info "🧪 Testing Scenario 2: Kafka Recovery"
        echo
        
        log_info "Starting Kafka (assuming it was stopped)..."
        docker start enefitresourceservice-kafka
        
        sleep 10
        wait_for_service "http://localhost:18081" "Kafka UI"
        
        log_info "Sending batch notification after recovery..."
        send_batch_notification
        
        log_info "✅ Check Kafka UI at http://localhost:18081"
        log_info "Navigate to Topics -> resource-updates to see messages"
        ;;
        
    "logs")
        log_info "📋 Showing recent application logs..."
        docker logs enefitresourceservice-app --tail=50
        ;;
        
    "status")
        log_info "📊 Service Status:"
        echo
        docker-compose ps
        echo
        
        log_info "🏥 Application Health:"
        curl -s http://localhost:18080/actuator/health | jq . || log_error "Application not responding"
        echo
        
        log_info "📈 Kafka UI Status:"
        curl -s http://localhost:18081 > /dev/null && log_info "Kafka UI is accessible" || log_warn "Kafka UI not accessible"
        ;;
        
    "cleanup")
        log_info "🧹 Cleaning up test environment..."
        docker-compose down
        log_info "Cleanup complete. Run 'docker-compose up -d' to restart."
        ;;
        
    "help"|*)
        echo "Usage: $0 [scenario1|scenario2|logs|status|cleanup]"
        echo
        echo "Commands:"
        echo "  scenario1  - Test Kafka failure during event publishing"
        echo "  scenario2  - Test Kafka recovery and event delivery"
        echo "  logs      - Show recent application logs"
        echo "  status    - Check service status and health"
        echo "  cleanup   - Stop all services"
        echo
        echo "Examples:"
        echo "  ./test-kafka-failure.sh scenario1"
        echo "  ./test-kafka-failure.sh logs"
        echo "  ./test-kafka-failure.sh status"
        ;;
esac
