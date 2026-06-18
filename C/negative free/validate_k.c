#include<stdio.h>
#include<stdlib.h>
#include<math.h>
#include<string.h>

int n,k, a[100],s[100];
long long int power[35];

//------------------------------------------------------
int Visit(char *visited) {
    long long int dec=0, dec2 = 0;

    for (int i=1; i<=n; i++) dec += power[i-1] * a[i];
    for (int i=1; i<=n; i++) dec2 += power[i-1] * ((k-a[i])%k);  // Negative
    
    if (dec != dec2 && (visited[dec] == 0 && visited[dec2] == 0)) {
        visited[dec] = visited[dec2] = 1;
        return 1;
    }
    return 0;
}
//------------------------------------------------------
int main(int argc, char **argv) {
    
    char c, *visited;
    int i,j,total;
    long long int L;
  
    sscanf(argv[1], "%d", &n);
    sscanf(argv[2], "%d", &k);
    sscanf(argv[3], "%lld", &L);
    
    // Initialize visited strings to 0
    power[0] = 1;
    for (i=1; i<=n; i++) power[i] = k * power[i-1];
    visited = (char *) malloc(sizeof(char) * power[n]);
    for (int i=0; i<power[n]; i++) visited[i] = 0;
    
    for (i=1; i<=L; i++) {
        scanf("%c",&c);
        if (i <=n) s[i] = a[i] = (c >= 'A') ? (c - 'A' + 10) : (c - '0');
        else {
            for (j=1; j<n; j++) a[j] = a[j+1];
            a[n] = (c >= 'A') ? (c - 'A' + 10) : (c - '0');
        }
        if (i >=n && !Visit(visited)) {
            printf("INVALID1 %i: ",i);
            for (j=1; j<=n; j++) printf("%d", a[j]);
            printf("\n");
            //exit(1);
        }
    }
    // Wraparound
    for (i=1; i<=n-1; i++) {
   	for (j=1; j<n; j++) a[j] = a[j+1];
        a[n] = s[i];
       	if (!Visit(visited)) { printf("INVALID2\n"); exit(1); }
    }
    printf("Valid\n");
}
