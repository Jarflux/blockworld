uniform sampler2D m_Terrain;
varying vec2 texCoord;
varying float lightLevel;

void main(){
    vec4 color = texture2D( m_Terrain, texCoord );
    vec4 black = vec4(0.0,0.0,0.0,1.0); 
    //gl_FragColor = color;
    gl_FragColor = mix(black,color,lightLevel); //0 dark //1 light 
}