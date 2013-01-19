uniform sampler2D m_Terrain;
varying vec2 texCoord;

void main(){
    gl_FragColor = texture2D( m_Terrain, texCoord );
}