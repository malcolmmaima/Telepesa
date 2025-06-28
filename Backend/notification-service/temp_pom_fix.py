import re

# Read the pom.xml file
with open('pom.xml', 'r') as file:
    content = file.read()

# Comment out shared library dependencies
content = re.sub(
    r'(<!-- Shared Libraries - Temporarily commented out for CI/CD fix -->\s+)(<dependency>\s+<groupId>com\.maelcolium\.telepesa</groupId>\s+<artifactId>common-models</artifactId>.*?</dependency>)',
    r'\1<!--\n        \2\n        -->',
    content,
    flags=re.DOTALL
)

content = re.sub(
    r'(<dependency>\s+<groupId>com\.maelcolium\.telepesa</groupId>\s+<artifactId>security-utils</artifactId>.*?</dependency>)',
    r'<!--\n        \1\n        -->',
    content,
    flags=re.DOTALL
)

content = re.sub(
    r'(<dependency>\s+<groupId>com\.maelcolium\.telepesa</groupId>\s+<artifactId>common-exceptions</artifactId>.*?</dependency>)',
    r'<!--\n        \1\n        -->',
    content,
    flags=re.DOTALL
)

# Comment out Spring Cloud dependencies
content = re.sub(
    r'(<!-- Spring Cloud OpenFeign for inter-service communication -->\s+)(<dependency>\s+<groupId>org\.springframework\.cloud</groupId>\s+<artifactId>spring-cloud-starter-openfeign</artifactId>.*?</dependency>)',
    r'\1<!--\n        \2\n        -->',
    content,
    flags=re.DOTALL
)

content = re.sub(
    r'(<!-- Circuit Breaker -->\s+)(<dependency>\s+<groupId>org\.springframework\.cloud</groupId>\s+<artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>.*?</dependency>)',
    r'\1<!--\n        \2\n        -->',
    content,
    flags=re.DOTALL
)

# Comment out Spring Cloud dependency management
content = re.sub(
    r'(<dependencyManagement>.*?</dependencyManagement>)',
    r'<!--\n    \1\n    -->',
    content,
    flags=re.DOTALL
)

# Write the updated content back
with open('pom.xml', 'w') as file:
    file.write(content)

print("POM.xml updated successfully")
