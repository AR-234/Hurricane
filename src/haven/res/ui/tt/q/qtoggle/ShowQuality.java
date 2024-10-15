/* Preprocessed source code */
/* $use: ui/tt/q/quality */
/* $use: ui/pag/toggle */

package haven.res.ui.tt.q.qtoggle;

import haven.*;
import haven.res.ui.tt.q.quality.*;
import haven.res.ui.pag.toggle.*;
import haven.MenuGrid.Pagina;

/* >pagina: ShowQuality$Fac */
@haven.FromResource(name = "ui/tt/q/qtoggle", version = 7)
public class ShowQuality extends MenuGrid.PagButton {
    public ShowQuality(Pagina pag) {
	super(pag);
    }

    public static class Fac implements Factory {
	public MenuGrid.PagButton make(Pagina pag) {
	    return(new ShowQuality(pag));
	}
    }

    /*
    public BufferedImage img() {return(res.layer(Resource.imgc, 1).scaled());}
    */

    public void use(MenuGrid.Interaction iact) {
	Utils.setprefb("qtoggle", Quality.show = !Quality.show);
	pag.scm.ui.msg("Quality display is now turned " + (Quality.show ? "on" : "off") + ".", null,
		       Audio.resclip(Quality.show ? Toggle.sfxon : Toggle.sfxoff));
    }

    public void drawmain(GOut g, GSprite spr) {
	super.drawmain(g, spr);
	g.image(Quality.show ? Toggle.on : Toggle.off, Coord.z);
    }
}
