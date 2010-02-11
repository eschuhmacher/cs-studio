package org.csstudio.trends.databrowser.model;

import java.io.PrintWriter;

import org.csstudio.apputil.xml.DOMHelper;
import org.csstudio.apputil.xml.XMLWriter;
import org.csstudio.trends.databrowser.preferences.Preferences;
import org.eclipse.swt.graphics.RGB;
import org.w3c.dom.Element;

/** Base of {@link PVItem} and {@link FormulaItem},
 *  i.e. the items held by the {@link Model}.
 * 
 *  @author Kay Kasemir
 */
abstract public class ModelItem
{
    /** Name by which the item is identified: PV name, formula */
    private String name;

    /** Model that contains this item or <code>null</code> while not
     *  assigned to a model
     */
    protected Model model = null;

    /** Preferred display name, used in plot legend */
    private String display_name;

    /** Show item's samples? */
    private boolean visible = true;
    
    /** RGB for item's color
     *  <p>
     *  Technically, swt.graphics.RGB adds a UI dependency to the Model.
     *  As long as the Model can still run without a Display
     *  or Shell, this might be OK.
     */
    private RGB rgb = null;
    
    /** Line width [pixel] */
    private int line_width = Preferences.getLineWidths();
    
    /** How to display the trace */
    private TraceType trace_type = TraceType.AREA;

    /** Y-Axis index */
    private int axis = 0;

    /** Initialize
     *  @param name Name of the PV or the formula
     */
    public ModelItem(final String name)
    {
        this.name = name;
        this.display_name = name;
    }
    
    /** @return Model that contains this item */
    public Model getModel()
    {
        return model;
    }

    /** Called by Model to add item to Model or remove it from model.
     *  Should not be called by other code!
     *  @param model Model to which item was added or <code>null</code> when removed
     */
    void setModel(final Model model)
    {
        if (this.model == model)
            throw new RuntimeException("Item re-assigned to same model: " + name); //$NON-NLS-1$
        this.model = model;
    }
    
    /** @return Name of this item (PV, Formula, ...) */
    public String getName()
    {
        return name;
    }

    /** @param new_name New item name
     *  @see #getName()
     *  @return <code>true</code> if name was actually changed
     *  @throws Exception on error (cannot create PV for new name, ...)
     *  
     */
    public boolean setName(String new_name) throws Exception
    {
        new_name = new_name.trim();
        if (new_name.equals(name))
            return false;
        name = new_name;
        fireItemLookChanged();
        return true;
    }

    /** @return Preferred display name, used in plot legend */
    public String getDisplayName()
    {
        return display_name;
    }

    /** @param new_display_name New display name
     *  @see #getDisplayName()
     */
    public void setDisplayName(String new_display_name)
    {
        new_display_name = new_display_name.trim();
        if (new_display_name.equals(display_name))
            return;
        display_name = new_display_name;
        fireItemLookChanged();
    }
    
    /** @return <code>true</code> if item should be displayed */
    public boolean isVisible()
    {
        return visible;
    }

    /** @param visible Should item be displayed? */
    public void setVisible(final boolean visible)
    {
        if (this.visible == visible)
            return;
        this.visible = visible;
        if (model != null)
            model.fireItemVisibilityChanged(this);
    }

    /** If (!) assigned to a model, inform it about a configuration change */
    protected void fireItemLookChanged()
    {
        if (model != null)
            model.fireItemLookChanged(this);
    }

    /** Get item's color.
     *  For new items, the color is <code>null</code> until it's
     *  either set via setColor() or by adding it to a {@link Model}.
     *  @return Item's color
     *  @see #setColor(RGB)
     */
    public RGB getColor()
    {
        return rgb;
    }

    /** @param new_rgb New color for this item */
    public void setColor(final RGB new_rgb)
    {
        if (new_rgb.equals(rgb))
            return;
        rgb = new_rgb;
        fireItemLookChanged();
    }

    /** @return Line width */
    public int getLineWidth()
    {
        return line_width;
    }

    /** @param width New line width */
    public void setLineWidth(int width)
    {
        if (width < 0)
            width = 0;
        if (width == this.line_width)
            return;
        line_width = width;
        fireItemLookChanged();
    }

    /** @return {@link TraceType} for displaying the trace */
    public TraceType getTraceType()
    {
        return trace_type;
    }

    /** @param trace_type New {@link TraceType} for displaying the trace */
    public void setTraceType(final TraceType trace_type)
    {
        if (this.trace_type == trace_type)
            return;
        this.trace_type = trace_type;
        fireItemLookChanged();
    }
    
    /** @return X-Axis index */
    public int getAxis()
    {
        return axis;
    }

    /** @param axis New X-Axis index */
    public void setAxis(final int axis)
    {
        if (axis == this.axis)
            return;
        this.axis = axis;
        fireItemLookChanged();
    }

    /** @return Samples held by this item */
    abstract public PlotSamples getSamples();
    
    @Override
    public String toString()
    {
        return name;
    }

    /** Write XML formatted item configuration
     *  @param writer PrintWriter
     */
    abstract public void write(final PrintWriter writer);

    /** Write XML configuration common to all Model Items
     *  @param writer PrintWriter
     */
    protected void writeCommonConfig(final PrintWriter writer)
    {
        XMLWriter.XML(writer, 3, Model.TAG_NAME, getName());
        XMLWriter.XML(writer, 3, Model.TAG_DISPLAYNAME, getDisplayName());
        XMLWriter.XML(writer, 3, Model.TAG_VISIBLE, Boolean.toString(isVisible()));
        XMLWriter.XML(writer, 3, Model.TAG_AXIS, getAxis());
        XMLWriter.XML(writer, 3, Model.TAG_LINEWIDTH, getLineWidth());
        Model.writeColor(writer, 3, Model.TAG_COLOR, getColor());
        XMLWriter.XML(writer, 3, Model.TAG_TRACE_TYPE, getTraceType().name());
    }

    /** Load common XML configuration elements into this item
     *  @param node XML document node for this item
     */
    protected void configureFromDocument(final Element node)
    {
        display_name = DOMHelper.getSubelementString(node, Model.TAG_DISPLAYNAME, display_name);
        visible = DOMHelper.getSubelementBoolean(node, Model.TAG_VISIBLE, true);
        axis = DOMHelper.getSubelementInt(node, Model.TAG_AXIS, axis);
        line_width = DOMHelper.getSubelementInt(node, Model.TAG_LINEWIDTH, line_width);
        rgb = Model.loadColorFromDocument(node);
        final String type = DOMHelper.getSubelementString(node, Model.TAG_TRACE_TYPE, TraceType.AREA.name());
        try
        {
            trace_type = TraceType.valueOf(type);
        }
        catch (Throwable ex)
        {
            trace_type = TraceType.AREA;
        }
    }
}
