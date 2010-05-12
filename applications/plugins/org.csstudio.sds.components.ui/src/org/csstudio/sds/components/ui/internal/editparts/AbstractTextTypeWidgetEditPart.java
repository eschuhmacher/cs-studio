/*
		* Copyright (c) 2010 Stiftung Deutsches Elektronen-Synchrotron,
		* Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
		*
		* THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS.
		* WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT
		NOT LIMITED
		* TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE
		AND
		* NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
		BE LIABLE
		* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
		CONTRACT,
		* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
		SOFTWARE OR
		* THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE
		DEFECTIVE
		* IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING,
		REPAIR OR
		* CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART
		OF THIS LICENSE.
		* NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS
		DISCLAIMER.
		* DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
		ENHANCEMENTS,
		* OR MODIFICATIONS.
		* THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION,
		MODIFICATION,
		* USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE
		DISTRIBUTION OF THIS
		* PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU
		MAY FIND A COPY
		* AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
		*/
package org.csstudio.sds.components.ui.internal.editparts;

import static org.csstudio.sds.model.AbstractTextTypeWidgetModel.PROP_PRECISION;
import static org.csstudio.sds.model.AbstractTextTypeWidgetModel.PROP_TEXT_TYPE;
import static org.csstudio.sds.model.AbstractWidgetModel.PROP_ALIASES;
import static org.csstudio.sds.model.AbstractWidgetModel.PROP_PRIMARY_PV;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.csstudio.sds.model.AbstractTextTypeWidgetModel;
import org.csstudio.sds.model.TextTypeEnum;
import org.csstudio.sds.ui.editparts.AbstractWidgetEditPart;
import org.csstudio.sds.ui.editparts.IWidgetPropertyChangeHandler;
import org.csstudio.sds.ui.figures.ITextFigure;
import org.csstudio.sds.util.ChannelReferenceValidationException;
import org.csstudio.sds.util.ChannelReferenceValidationUtil;
import org.eclipse.draw2d.IFigure;

/**
 * Give two Properties for format a String representation.
 *
 *
 * @author hrickens
 * @author $Author$
 * @version $Revision$
 * @since 11.05.2010
 */
public abstract class AbstractTextTypeWidgetEditPart extends AbstractWidgetEditPart {

    private final NumberFormat numberFormat = NumberFormat.getInstance();

    /**
     *
     * {@inheritDoc}
     */
    @Override
    protected void registerPropertyChangeHandlers() {

        IWidgetPropertyChangeHandler handle = new IWidgetPropertyChangeHandler() {
            public boolean handleChange(final Object oldValue, final Object newValue, final IFigure refreshableFigure) {
                ITextFigure labelFigure = (ITextFigure) refreshableFigure;
                labelFigure.setTextValue(determineLabel(null));
                return true;
            }
        };
        setPropertyChangeHandler(PROP_TEXT_TYPE, handle);

        // precision
        IWidgetPropertyChangeHandler handler = new IWidgetPropertyChangeHandler() {
            public boolean handleChange(final Object oldValue, final Object newValue, final IFigure refreshableFigure) {
                ITextFigure labelFigure = (ITextFigure) refreshableFigure;
                labelFigure.setTextValue(determineLabel(PROP_PRECISION));
                return true;
            }
        };
        setPropertyChangeHandler(PROP_PRECISION, handler);

        // aliases
        IWidgetPropertyChangeHandler aliasHandler = new IWidgetPropertyChangeHandler() {
            public boolean handleChange(final Object oldValue, final Object newValue, final IFigure refreshableFigure) {
                ITextFigure labelFigure = (ITextFigure) refreshableFigure;
                labelFigure.setTextValue(determineLabel(PROP_ALIASES));
                return true;
            }
        };
        setPropertyChangeHandler(PROP_ALIASES, aliasHandler);
        // primary pv
        IWidgetPropertyChangeHandler pvHandler = new IWidgetPropertyChangeHandler() {
            public boolean handleChange(final Object oldValue, final Object newValue, final IFigure refreshableFigure) {
                ITextFigure labelFigure = (ITextFigure) refreshableFigure;
                labelFigure.setTextValue(determineLabel(PROP_ALIASES));
                return true;
            }
        };
        setPropertyChangeHandler(PROP_PRIMARY_PV, pvHandler);

    }

    /**
     * Format a String on the base of the properties PROP_TEXT_TYPE and PROP_PRECISION.
     *
     * @param updatedPropertyId the Property that was updated
     * @return the new string value
     */
//    abstract String determineLabel(final String updatedPropertyId);
    protected final String determineLabel(final String updatedPropertyId) {
        AbstractTextTypeWidgetModel model = (AbstractTextTypeWidgetModel) getCastedModel();

        TextTypeEnum type = model.getValueType();
        String text = model.getStringValue();

        String toprint = "none";

        switch (type) {
            case TEXT:
                if ( (updatedPropertyId == null)
                        || updatedPropertyId.equals(model.getStringValueID())) {
                    toprint = text;
                }
                break;
            case DOUBLE:
                if ( (updatedPropertyId == null)
                        || updatedPropertyId.equals(model.getStringValueID())
                        || updatedPropertyId.equals(AbstractTextTypeWidgetModel.PROP_PRECISION)) {
                    try {
                        double d = Double.parseDouble(text);
                        numberFormat.setMaximumFractionDigits(model.getPrecision());
                        numberFormat.setMinimumFractionDigits(model.getPrecision());
                        toprint = numberFormat.format(d);
                    } catch (Exception e) {
                        toprint = text;
                    }
                }
                break;
            case ALIAS:
                if ( (updatedPropertyId == null)
                        || updatedPropertyId.equals(AbstractTextTypeWidgetModel.PROP_ALIASES)
                        || updatedPropertyId.equals(AbstractTextTypeWidgetModel.PROP_PRIMARY_PV)) {
                    try {
                        toprint = ChannelReferenceValidationUtil.createCanonicalName(model
                                .getPrimaryPV(), model.getAllInheritedAliases());
                    } catch (ChannelReferenceValidationException e) {
                        toprint = model.getPrimaryPV();
                    }
                }
                break;
            case HEX:
                if ( (updatedPropertyId == null)
                        || updatedPropertyId.equals(model.getStringValueID())) {
                    try {
                        long l = Long.parseLong(text);
                        toprint = Long.toHexString(l);
                    } catch (Exception e1) {
                        try {
                            double d = Double.parseDouble(text);
                            toprint = Double.toHexString(d);
                        } catch (Exception e2) {
                            toprint = text;
                        }
                    }
                }
                break;
            case EXP:
                if ( (updatedPropertyId == null)
                        || updatedPropertyId.equals(model.getStringValueID())
                        || updatedPropertyId.equals(AbstractTextTypeWidgetModel.PROP_PRECISION)) {
                    try {
                        String pattern = "0.";
                        for (int i = 0; i < model.getPrecision(); i++) {
                            if (i == 0) {
                                pattern = pattern.concat("0");
                            } else {
                                pattern = pattern.concat("#");
                            }
                        }
                        pattern = pattern.concat("E00");
                        DecimalFormat expFormat = new DecimalFormat(pattern);
                        double d = Double.parseDouble(text);
                        toprint = expFormat.format(d);
                    } catch (Exception e) {
                        toprint = text;
                    }
                }
                break;
            default:
                toprint = "unknown value type";
        }
        return toprint;
    }
}
