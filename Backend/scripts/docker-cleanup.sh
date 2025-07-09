#!/bin/bash

# Telepesa Docker Cleanup Script
# Comprehensive cleanup options for Docker containers, volumes, and images

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ASCII Banner
echo -e "${CYAN}"
cat << "EOF"
╔══════════════════════════════════════════════════════════════════════════════╗
║                                                                              ║
║  ████████╗███████╗██╗     ███████╗██████╗ ███████╗███████╗ █████╗             ║
║  ╚══██╔══╝██╔════╝██║     ██╔════╝██╔══██╗██╔════╝██╔════╝██╔══██╗            ║
║     ██║   █████╗  ██║     █████╗  ██████╔╝█████╗  ███████╗███████║            ║
║     ██║   ██╔══╝  ██║     ██╔══╝  ██╔═══╝ ██╔══╝  ╚════██║██╔══██║            ║
║     ██║   ███████╗███████╗███████╗██║     ███████╗███████║██║  ██║            ║
║     ╚═╝   ╚══════╝╚══════╝╚══════╝╚═╝     ╚══════╝╚══════╝╚═╝  ╚═╝            ║
║                                                                              ║
║                     🧹 Docker Cleanup System 🧹                             ║
║                      🗑️  Resource Management 🗑️                             ║
║                                                                              ║
╚══════════════════════════════════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

# Function to print status messages
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${PURPLE}[STEP]${NC} $1"
}

# Function to display current system status
show_current_status() {
    print_step "Current Docker System Status"
    
    echo -e "${CYAN}📊 Docker Resources:${NC}"
    
    # Show containers
    local total_containers=$(docker ps -a --format "table {{.Names}}" | grep -c telepesa || echo "0")
    local running_containers=$(docker ps --format "table {{.Names}}" | grep -c telepesa || echo "0")
    echo "   • Telepesa Containers: $running_containers running / $total_containers total"
    
    # Show images
    local telepesa_images=$(docker images --format "table {{.Repository}}" | grep -c telepesa || echo "0")
    echo "   • Telepesa Images:     $telepesa_images"
    
    # Show volumes
    local telepesa_volumes=$(docker volume ls --format "table {{.Name}}" | grep -c docker-compose || echo "0")
    echo "   • Telepesa Volumes:    $telepesa_volumes"
    
    # Show networks
    local telepesa_networks=$(docker network ls --format "table {{.Name}}" | grep -c docker-compose || echo "0")
    echo "   • Telepesa Networks:   $telepesa_networks"
    
    # Show disk usage
    echo ""
    echo -e "${YELLOW}💾 Docker Disk Usage:${NC}"
    docker system df
    
    echo ""
}

# Function to display cleanup options
show_cleanup_options() {
    echo -e "${CYAN}🧹 Cleanup Options:${NC}"
    echo ""
    echo "1. 🛑 Stop Services Only"
    echo "   └─ Stop all running containers (keeps data)"
    echo ""
    echo "2. 🗑️  Remove Containers"
    echo "   └─ Stop and remove all containers (keeps images and volumes)"
    echo ""
    echo "3. 🧽 Full Cleanup"
    echo "   └─ Remove containers, images, and networks (keeps volumes)"
    echo ""
    echo "4. ☢️  Nuclear Cleanup"
    echo "   └─ Remove EVERYTHING (containers, images, volumes, networks)"
    echo ""
    echo "5. 📊 Show Status Only"
    echo "   └─ Display current system status and exit"
    echo ""
    echo "6. ❌ Cancel"
    echo "   └─ Exit without making changes"
    echo ""
}

# Function to stop services only
stop_services() {
    print_step "Stopping Telepesa services..."
    
    cd docker-compose
    
    if docker-compose ps | grep -q "Up"; then
        docker-compose stop
        print_success "All services stopped successfully"
    else
        print_warning "No running services found"
    fi
    
    cd ..
}

# Function to remove containers
remove_containers() {
    print_step "Removing Telepesa containers..."
    
    cd docker-compose
    
    # Stop and remove containers
    if docker-compose ps -a | grep -q telepesa; then
        docker-compose down
        print_success "All containers removed successfully"
    else
        print_warning "No containers found to remove"
    fi
    
    cd ..
}

# Function to full cleanup (containers, images, networks)
full_cleanup() {
    print_step "Performing full cleanup (containers, images, networks)..."
    
    cd docker-compose
    
    # Stop and remove containers
    if docker-compose ps -a | grep -q telepesa; then
        docker-compose down
        print_success "Containers removed"
    fi
    
    cd ..
    
    # Remove Telepesa images
    print_status "Removing Telepesa images..."
    local telepesa_images=$(docker images --format "{{.Repository}}:{{.Tag}}" | grep telepesa || true)
    
    if [[ -n "$telepesa_images" ]]; then
        echo "$telepesa_images" | xargs docker rmi -f
        print_success "Telepesa images removed"
    else
        print_warning "No Telepesa images found"
    fi
    
    # Remove unused networks
    print_status "Removing unused networks..."
    docker network prune -f
    print_success "Unused networks removed"
}

# Function to nuclear cleanup (everything)
nuclear_cleanup() {
    print_step "Performing nuclear cleanup (EVERYTHING)..."
    
    cd docker-compose
    
    # Stop and remove everything
    if docker-compose ps -a | grep -q telepesa; then
        docker-compose down -v --remove-orphans
        print_success "Containers and volumes removed"
    fi
    
    cd ..
    
    # Remove all Telepesa images
    print_status "Removing all Telepesa images..."
    local all_images=$(docker images --format "{{.Repository}}:{{.Tag}}" | grep -E "(telepesa|postgres|mongo|redis|kafka|zookeeper|zipkin)" || true)
    
    if [[ -n "$all_images" ]]; then
        echo "$all_images" | xargs docker rmi -f
        print_success "All related images removed"
    else
        print_warning "No related images found"
    fi
    
    # Remove all volumes
    print_status "Removing all volumes..."
    local telepesa_volumes=$(docker volume ls --format "{{.Name}}" | grep docker-compose || true)
    
    if [[ -n "$telepesa_volumes" ]]; then
        echo "$telepesa_volumes" | xargs docker volume rm -f
        print_success "All volumes removed"
    else
        print_warning "No volumes found"
    fi
    
    # Clean up system
    print_status "Cleaning up Docker system..."
    docker system prune -a -f --volumes
    print_success "Docker system cleaned"
}

# Function to confirm destructive actions
confirm_action() {
    local action_name="$1"
    local is_destructive="$2"
    
    if [[ "$is_destructive" == "true" ]]; then
        echo ""
        print_warning "⚠️  This action is DESTRUCTIVE and cannot be undone!"
        print_warning "⚠️  It will permanently delete Docker resources."
        echo ""
        echo -n "Are you sure you want to proceed with $action_name? (type 'yes' to confirm): "
        read confirmation
        
        if [[ "$confirmation" != "yes" ]]; then
            print_status "Action cancelled by user"
            return 1
        fi
    else
        echo ""
        echo -n "Proceed with $action_name? (y/N): "
        read confirmation
        
        if [[ "$confirmation" != "y" && "$confirmation" != "Y" ]]; then
            print_status "Action cancelled by user"
            return 1
        fi
    fi
    
    return 0
}

# Function to show post-cleanup status
show_post_cleanup_status() {
    echo ""
    print_step "Post-cleanup Status"
    
    # Show remaining containers
    local remaining_containers=$(docker ps -a --format "table {{.Names}}" | grep -c telepesa || echo "0")
    echo "   • Remaining Containers: $remaining_containers"
    
    # Show remaining images
    local remaining_images=$(docker images --format "table {{.Repository}}" | grep -c telepesa || echo "0")
    echo "   • Remaining Images:     $remaining_images"
    
    # Show remaining volumes
    local remaining_volumes=$(docker volume ls --format "table {{.Name}}" | grep -c docker-compose || echo "0")
    echo "   • Remaining Volumes:    $remaining_volumes"
    
    echo ""
    echo -e "${GREEN}✅ Cleanup completed successfully!${NC}"
    
    if [[ $remaining_containers -eq 0 && $remaining_images -eq 0 && $remaining_volumes -eq 0 ]]; then
        echo -e "${GREEN}🎉 All Telepesa Docker resources have been removed.${NC}"
    else
        echo -e "${YELLOW}ℹ️  Some resources remain (this may be intentional).${NC}"
    fi
}

# Main execution
main() {
    print_status "Telepesa Docker Cleanup Tool"
    
    # Change to Backend directory if not already there
    if [[ ! -f "docker-compose/docker-compose.yml" ]]; then
        if [[ -f "../docker-compose/docker-compose.yml" ]]; then
            cd ..
        else
            print_error "Cannot find docker-compose.yml. Please run from Backend directory."
            exit 1
        fi
    fi
    
    # Show current status
    show_current_status
    
    # Show cleanup options
    show_cleanup_options
    
    # Get user choice
    echo -n "Select cleanup option (1-6): "
    read choice
    
    case "$choice" in
        "1")
            if confirm_action "stop services" "false"; then
                stop_services
                print_success "Services stopped. Data preserved."
            fi
            ;;
        "2")
            if confirm_action "remove containers" "false"; then
                remove_containers
                show_post_cleanup_status
            fi
            ;;
        "3")
            if confirm_action "full cleanup" "true"; then
                full_cleanup
                show_post_cleanup_status
            fi
            ;;
        "4")
            if confirm_action "nuclear cleanup" "true"; then
                nuclear_cleanup
                show_post_cleanup_status
            fi
            ;;
        "5")
            print_status "Current status displayed above. No changes made."
            ;;
        "6")
            print_status "Cleanup cancelled. No changes made."
            ;;
        *)
            print_error "Invalid option: $choice"
            print_status "Please run the script again and select a valid option (1-6)."
            exit 1
            ;;
    esac
    
    echo ""
    echo -e "${BLUE}📝 Useful Commands:${NC}"
    echo "   • Start services:      ./scripts/docker-startup.sh"
    echo "   • Monitor services:    ./scripts/docker-monitor.sh"
    echo "   • Run tests:          ./scripts/gateway-e2e-test.sh"
    echo "   • Check status:       docker-compose ps"
    echo "   • View logs:          docker-compose logs -f [service-name]"
    
    print_success "Cleanup operation completed!"
}

# Handle interrupts gracefully
trap 'print_warning "Cleanup interrupted."; exit 1' INT TERM

# Execute main function
main "$@" 