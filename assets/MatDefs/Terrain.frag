uniform sampler2D m_Terrain;
uniform float m_DayAlpha;
varying vec2 texCoord;
varying float naturalLightLevel;
varying float artificialLightLevel;

void main(){
    vec4 color = texture2D( m_Terrain, texCoord );
    vec4 black = vec4(0.0,0.0,0.0,1.0);
    gl_FragColor = mix(black,color,min(1,(naturalLightLevel*m_DayAlpha) + artificialLightLevel)); 
}